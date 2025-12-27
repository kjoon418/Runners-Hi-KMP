package good.space.runnershi.model.dto.auth

import good.space.runnershi.model.domain.auth.Sex
import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val name: String,
    val sex: Sex
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class TokenRefreshRequest(
    val refreshToken: String
)
