package good.space.runnershi.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val nickname: String
)

