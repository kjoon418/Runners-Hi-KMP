package good.space.runnershi.model.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class QuestResponse(
    val title: String,
    val exp: Long,
    val isCompleted: Boolean
)
