package good.space.runnershi.repository

import good.space.runnershi.model.dto.LoginRequest
import good.space.runnershi.model.dto.LoginResponse
import good.space.runnershi.model.dto.SignUpRequest

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<LoginResponse>
    suspend fun signUp(request: SignUpRequest): Result<LoginResponse>
}