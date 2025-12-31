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
    val newBadges: List<newBadgeInfo>,
    val dailyQuests: List<dailyQuestInfo>,
    val avatar: AvatarInfo,
    val unlockedAvatars: List<NewUnlockedAvatarInfo>,
    val userExpProgressPercentage: Int,
    val completedQuests: List<dailyQuestInfo>,
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
