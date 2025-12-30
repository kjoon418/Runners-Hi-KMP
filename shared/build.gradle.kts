@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.buildkonfig)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

kotlin {
    applyDefaultHierarchyTemplate()

    jvmToolchain(17)

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    
    iosArm64()
    iosSimulatorArm64()
    jvm()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.auth)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.multiplatform.settings)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.androidx.navigation.compose)

                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview) // Preview 공통 지원
                implementation(compose.materialIconsExtended)

                // Koin Multiplatform
                implementation(libs.koin.core)
                implementation(libs.koin.compose)            // Composable에서 Koin 사용
                implementation(libs.koin.compose.viewmodel)  // CMP용 ViewModel 지원
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        
        val jvmMain by getting
        
        val androidMain by getting {
            dependencies {
                implementation(libs.sqldelight.android.driver)
                implementation(libs.ktor.client.android)
                implementation(libs.play.services.location)

                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.lifecycle.viewmodel.compose) // ViewModel()
                implementation(libs.androidx.security.crypto)

                // 안드로이드 스튜디오 미리보기용 (디버그)
                implementation(compose.uiTooling)
            }
        }
        
        val jvmTest by getting
        
        val iosMain by getting {
            dependencies {
                implementation(libs.ktor.client.ios)
                implementation(libs.sqldelight.native.driver)
            }
        }
        
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
    }
}

android {
    namespace = "good.space.runnershi.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("good.space.runnershi.shared.database")
        }
    }
}

buildkonfig {
    packageName = "good.space.runnershi"

    // 모든 타겟 공통 설정
    defaultConfigs {
        val serverUrl = localProperties.getProperty("BASE_URL") ?: "http://localhost:8080"

        buildConfigField(FieldSpec.Type.STRING, "BASE_URL", serverUrl)
    }

    // 안드로이드용 오버라이드
    defaultConfigs("android") {
        // 안드로이드는 localhost 대신 10.0.2.2가 필요함
        val androidUrl = localProperties.getProperty("BASE_URL") ?: "http://10.0.2.2:8080"

        buildConfigField(FieldSpec.Type.STRING, "BASE_URL", androidUrl)
    }
}
