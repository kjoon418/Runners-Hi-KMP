package good.space.runnershi.model.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String?
)

@Serializable
data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String?
)
