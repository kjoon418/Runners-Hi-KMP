package good.space.runnershi.repository

import good.space.runnershi.model.dto.auth.LoginRequest
import good.space.runnershi.model.dto.auth.SignUpRequest
import good.space.runnershi.model.dto.auth.TokenResponse

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<TokenResponse>
    suspend fun signUp(request: SignUpRequest): Result<TokenResponse>
    suspend fun logout(): Result<Unit>

    suspend fun checkEmailAvailability(email: String): Result<Boolean>

    suspend fun checkNameAvailability(name: String): Result<Boolean>
}
