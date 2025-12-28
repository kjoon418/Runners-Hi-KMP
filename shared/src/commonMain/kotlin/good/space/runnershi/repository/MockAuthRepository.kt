package good.space.runnershi.repository

import good.space.runnershi.model.dto.auth.LoginRequest
import good.space.runnershi.model.dto.auth.LoginResponse
import good.space.runnershi.model.dto.auth.SignUpRequest
import kotlinx.coroutines.delay

class MockAuthRepository : AuthRepository {

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        delay(1000) // 네트워크 지연 시뮬레이션
        return Result.success(LoginResponse("mock_access_token", "mock_refresh_token"))
    }

    override suspend fun signUp(request: SignUpRequest): Result<LoginResponse> {
        delay(1500) // 회원가입은 좀 더 오래 걸리는 척
        return Result.success(
            LoginResponse(
                accessToken = "mock_access_token_signup",
                refreshToken = "mock_refresh_token_signup"
            )
        )
    }

    override suspend fun logout(): Result<Unit> {
        return Result.success(Unit)
    }

    /**
     * 이메일 중복 확인 Mock
     * 테스트 방법: "duplicate@test.com"을 입력하면 이미 사용 중인 것으로 간주
     */
    override suspend fun checkEmailAvailability(email: String): Result<Boolean> {
        delay(800) // 지연 시간

        // 1. 에러 테스트: "error"가 포함되면 네트워크 에러 발생
        if (email.contains("error")) {
            return Result.failure(Exception("Mock Network Error"))
        }

        // 2. 중복 테스트: "duplicate@test.com"은 이미 사용 중(false) 리턴
        if (email == "duplicate@test.com") {
            return Result.success(false)
        }

        // 3. 그 외: 사용 가능(true)
        return Result.success(true)
    }

    /**
     * 이름 중복 확인 Mock
     * 테스트 방법: "runner"라는 이름은 이미 사용 중인 것으로 간주
     */
    override suspend fun checkNameAvailability(name: String): Result<Boolean> {
        delay(800)

        // 1. 중복 테스트: "runner"는 이미 사용 중
        if (name == "runner") {
            return Result.success(false)
        }

        // 2. 그 외: 사용 가능
        return Result.success(true)
    }
}

