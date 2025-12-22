package good.space.runnershi.model.dto.running

import kotlinx.serialization.Serializable

@Serializable
data class UpdatedUserResponse(
    val userId: Long,
    val userExp: Long,
    val totalRunningDays: Long,
    val badges: List<String>,
    val newBadges: List<newBadgeInfo>,
    val dailyQuests: List<dailyQuestInfo>
)

@Serializable
data class newBadgeInfo(
    val name: String,
    val exp: Long
)

@Serializable
data class dailyQuestInfo(
    val title: String,
    val exp: Long,
    val isComplete: Boolean
)
