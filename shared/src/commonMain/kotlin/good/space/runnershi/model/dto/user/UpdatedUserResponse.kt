package good.space.runnershi.model.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class UpdatedUserResponse(
    val userId: Long,
    val userExp: Long,
    val level: Int,
    val requiredExpForLevel: Long,
    val totalRunningDays: Long,
    val newBadges: List<BadgeInfo>,
    val avatar: AvatarInfo,
    val unlockedAvatars: List<UnlockedItem>,
    val userExpProgressPercentage: Int,
    val completedQuests: List<DailyQuestInfo>,
    val runningExp: Long
)

@Serializable
data class BadgeInfo(
    val title: String ,
    val description: String,
    val exp: Long
)

@Serializable
data class DailyQuestInfo(
    val title: String,
    val exp: Long,
    val isComplete: Boolean
)
