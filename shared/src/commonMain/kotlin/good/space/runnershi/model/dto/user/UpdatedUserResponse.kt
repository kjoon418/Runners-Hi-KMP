package good.space.runnershi.model.dto.running

import good.space.runnershi.model.dto.user.AvatarInfo
import good.space.runnershi.model.dto.user.NewUnlockedAvatarInfo
import kotlinx.serialization.Serializable

@Serializable
data class UpdatedUserResponse(
    val userId: Long,
    val userExp: Long,
    val level: Int,
    val totalRunningDays: Long,
    val badges: List<String>,
    val newBadges: List<NewBadgeInfo>,
    val dailyQuests: List<DailyQuestInfo>,
    val avatar: AvatarInfo,
    val unlockedAvatars: List<NewUnlockedAvatarInfo>,
    val userExpProgressPercentage: Int,
    val completedQuests: List<DailyQuestInfo>,
)

@Serializable
data class NewBadgeInfo(
    val name: String,
    val exp: Long
)

@Serializable
data class DailyQuestInfo(
    val title: String,
    val exp: Long,
    val isComplete: Boolean
)
