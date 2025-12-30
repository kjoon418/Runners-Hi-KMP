package good.space.runnershi.di

import good.space.runnershi.BuildKonfig
import good.space.runnershi.auth.TokenStorage
import good.space.runnershi.network.ApiClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val networkModule = module {
    
    // 1. Base URL 정의 (상수로 관리하거나 BuildConfig에서 가져옴)
    single(named("BaseUrl")) { BuildKonfig.BASE_URL }

    // 2. 인증이 필요 없는 HttpClient (Public)
    single(named("PublicClient")) {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 10_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 10_000
            }
            install(Logging) {
                level = LogLevel.ALL
            }
            // BaseURL을 여기서 미리 설정해두면 Repository가 편해집니다
            defaultRequest {
                url(get<String>(named("BaseUrl")))
            }
        }
    }

    // 3. 인증이 필요한 HttpClient (Authenticated) - 토큰 인터셉터 등 추가
    single(named("AuthClient")) {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 10_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 10_000
            }
            // 여기에 AuthPlugin이나 헤더 주입 로직 추가 가능
            defaultRequest {
                url(get<String>(named("BaseUrl")))
            }
        }
    }

    // 4. ApiClient (인증이 필요한 요청용)
    single {
        ApiClient(
            tokenStorage = get<TokenStorage>(),
            baseUrl = get<String>(named("BaseUrl"))
        )
    }
}
