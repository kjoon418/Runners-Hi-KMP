plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlin.jpa)
}

group = "good.space.runnershi"
version = "1.0.0"

dependencies {
    implementation(libs.mysql.connector)
    implementation(projects.shared)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.spring.boot.starter.data.jpa)
    testImplementation(libs.spring.boot.starter.test)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(libs.kotlin.reflect)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
}


kotlin {
    jvmToolchain(17)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
