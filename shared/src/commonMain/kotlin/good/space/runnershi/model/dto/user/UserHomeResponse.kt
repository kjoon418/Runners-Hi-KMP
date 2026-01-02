package good.space.runnershi.model.dto.user

import good.space.runnershi.model.domain.auth.Sex
import kotlinx.serialization.Serializable

@Serializable
data class UserHomeResponse(
    val userId: Long,
    val name: String,
    val userExp: Long,
    val totalDistance: Double,
    val totalRunningDays: Long,
    val achievements: List<BadgeInfo>,
    val dailyQuests: List<HomeQuestInfo>,
    val sex: Sex,
    val level: Int,
    val avatars: AvatarInfo
)

@Serializable
data class HomeQuestInfo(
    val questId: Long,
    val title: String,
    val level: Long,
    val exp: Long,
    val isCompleted: Boolean
)
