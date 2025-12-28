package good.space.runnershi.repository

import good.space.runnershi.model.dto.auth.LoginRequest
import good.space.runnershi.model.dto.auth.LoginResponse
import good.space.runnershi.model.dto.auth.SignUpRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class AuthRepositoryImpl(
    private val httpClient: HttpClient, // 인증 없는 요청용 (login, signup)
    private val baseUrl: String,
    private val authenticatedHttpClient: HttpClient? = null // 인증 필요한 요청용 (logout) - 선택적
) : AuthRepository {

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = httpClient.post("$baseUrl/api/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            if (response.status == HttpStatusCode.OK) {
                val tokenResponse = response.body<LoginResponse>()
                Result.success(tokenResponse)
            } else {
                Result.failure(Exception("로그인 실패: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(request: SignUpRequest): Result<LoginResponse> {
        return try {
            // 1. 회원가입 요청
            val signUpResponse = httpClient.post("$baseUrl/api/v1/auth/signup") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            if (signUpResponse.status != HttpStatusCode.Created) {
                return Result.failure(Exception("회원가입 실패: ${signUpResponse.status}"))
            }
            
            // 2. 회원가입 성공 후 자동 로그인 (UX 개선)
            val loginRequest = LoginRequest(
                email = request.email,
                password = request.password
            )
            
            val loginResponse = httpClient.post("$baseUrl/api/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(loginRequest)
            }
            
            if (loginResponse.status == HttpStatusCode.OK) {
                val tokenResponse = loginResponse.body<LoginResponse>()
                Result.success(tokenResponse)
            } else {
                Result.failure(Exception("회원가입은 성공했으나 자동 로그인 실패: ${loginResponse.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            // 인증이 필요한 요청이므로 authenticatedHttpClient 사용 (없으면 httpClient 사용)
            val client = authenticatedHttpClient ?: httpClient
            val response = client.post("$baseUrl/api/v1/auth/logout")
            
            // 2XX 상태코드면 성공 (200 OK 또는 204 NO CONTENT 등)
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("로그아웃 실패: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkEmailAvailability(email: String): Result<Boolean> {
        return try {
            // TODO: 실제 API 엔드포인트로 수정
            val response = httpClient.get("$baseUrl/api/v1/auth/check-email") {
                parameter("email", email)
            }

            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.NoContent -> {
                    Result.success(true)
                }
                HttpStatusCode.Conflict -> {
                    Result.success(false)
                }
                else -> {
                    Result.failure(Exception("서버 오류: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkNameAvailability(name: String): Result<Boolean> {
        return try {
            // TODO: 실제 API 엔드포인트로 수정
            val response = httpClient.get("$baseUrl/api/v1/auth/check-name") {
                parameter("name", name)
            }

            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.NoContent -> {
                    Result.success(true)
                }
                HttpStatusCode.Conflict -> {
                    Result.success(false)
                }
                else -> {
                    Result.failure(Exception("서버 오류: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

