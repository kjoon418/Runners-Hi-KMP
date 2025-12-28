package good.space.runnershi.repository

import good.space.runnershi.model.dto.auth.LoginRequest
import good.space.runnershi.model.dto.auth.LoginResponse
import good.space.runnershi.model.dto.auth.SignUpRequest

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<LoginResponse>
    suspend fun signUp(request: SignUpRequest): Result<LoginResponse>
    suspend fun logout(): Result<Unit>

    suspend fun checkEmailAvailability(email: String): Result<Boolean>

    suspend fun checkNameAvailability(name: String): Result<Boolean>
}
