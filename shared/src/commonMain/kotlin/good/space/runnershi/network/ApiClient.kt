package good.space.runnershi.network

import good.space.runnershi.auth.TokenStorage
import good.space.runnershi.model.dto.auth.TokenRefreshRequest
import good.space.runnershi.model.dto.auth.TokenRefreshResponse
import good.space.runnershi.model.dto.auth.TokenResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json

class ApiClient(
    private val tokenStorage: TokenStorage,
    val baseUrl: String = "https://api.runnershi.com" // TODO: 환경변수로 관리
) {
    // [핵심] 로그아웃 이벤트를 UI로 전파하기 위한 Flow
    private val _authErrorFlow = MutableSharedFlow<Unit>()
    val authErrorFlow = _authErrorFlow.asSharedFlow()

    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { 
                prettyPrint = true 
                ignoreUnknownKeys = true 
            })
        }

        install(Logging) {
            level = LogLevel.ALL
        }

        // 🔐 [핵심] JWT 인증 플러그인 설정
        install(Auth) {
            bearer {
                // 1. 요청 보낼 때 토큰 꺼내서 헤더에 넣기
                loadTokens {
                    val access = tokenStorage.getAccessToken()
                    val refresh = tokenStorage.getRefreshToken()
                    if (access != null && refresh != null) {
                        BearerTokens(access, refresh)
                    } else {
                        null
                    }
                }

                // 2. 401 에러 발생 시 토큰 갱신 시도 (Refresh Token Logic)
                refreshTokens {
                    val refreshToken = tokenStorage.getRefreshToken() ?: return@refreshTokens null

                    try {
                        // [갱신 API 호출]
                        // 주의: 여기선 client가 아닌 별도의 client를 쓰거나, Auth가 없는 요청을 보내야 함
                        val newTokens = refreshAccessTokenApi(refreshToken) 
                        
                        // 3. 갱신 성공 시 저장 및 리턴 -> Ktor가 알아서 원래 요청 재시도함
                        tokenStorage.saveTokens(newTokens.accessToken, newTokens.refreshToken)
                        BearerTokens(newTokens.accessToken, newTokens.refreshToken)
                        
                    } catch (_: Exception) {
                        // 4. [갱신 실패] -> 로그아웃 처리
                        tokenStorage.clearTokens()
                        _authErrorFlow.emit(Unit) // UI에 "로그인 화면으로 이동해"라고 알림
                        null
                    }
                }
            }
        }
    }

    // 실제 갱신 API 호출
    private suspend fun refreshAccessTokenApi(refreshToken: String): TokenResponse {
        // Ktor Client를 새로 만들거나, Auth 설정을 뺀 요청을 보내야 무한 루프 방지
        val refreshClient = HttpClient { 
            install(ContentNegotiation) { 
                json(Json { 
                    ignoreUnknownKeys = true 
                }) 
            }
        }
        
        val response = refreshClient.post("$baseUrl/api/v1/auth/refresh") {
            setBody(TokenRefreshRequest(refreshToken))
            contentType(ContentType.Application.Json)
        }
        
        // 서버는 TokenRefreshResponse(accessToken만)를 반환하므로, 기존 refreshToken과 함께 TokenResponse로 변환
        val refreshResponse = response.body<TokenRefreshResponse>()
        return TokenResponse(
            accessToken = refreshResponse.accessToken,
            refreshToken = refreshToken // refreshToken은 갱신되지 않고 그대로 유지
        )
    }
}

