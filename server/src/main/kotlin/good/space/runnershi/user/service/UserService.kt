package good.space.runnershi.user.service

import good.space.runnershi.global.exception.UserNotFoundException
import good.space.runnershi.model.dto.user.BadgeInfo
import good.space.runnershi.model.dto.user.AvatarInfo
import good.space.runnershi.model.dto.user.AvatarResponse
import good.space.runnershi.model.dto.user.AvatarUpdateRequest
import good.space.runnershi.model.dto.user.HomeQuestInfo
import good.space.runnershi.model.dto.user.UserHomeResponse
import good.space.runnershi.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class UserService (
    private val userRepository: UserRepository
) {
    @Transactional
    fun loadHomeData(userId: Long): UserHomeResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found with id: $userId") }

        user.refreshDailyQuestsIfNeeded()

        return UserHomeResponse(
            userId = user.id!!,
            name = user.name,
            userExp = user.exp,
            totalDistance = user.totalDistanceMeters,
            totalRunningDays = user.totalRunningDays,

            dailyQuests = user.dailyQuests.map { status ->
                HomeQuestInfo(
                    questId = status.quest.questId,
                    title = status.quest.title,
                    level = status.quest.level,
                    exp = status.quest.exp,
                    isCompleted = status.isCompleted
                )
            },
            achievements = user.achievements.map { achievement ->
                BadgeInfo(
                    title = achievement.title,
                    description = achievement.description,
                    exp = achievement.exp,
                )
            },
            sex = user.sex,
            level = user.level,
            avatars = AvatarInfo(
                head = user.avatar.head,
                top = user.avatar.top,
                bottom = user.avatar.bottom,
                shoes = user.avatar.shoes
            )
            )
    }

    @Transactional
    fun changeAvatar(userId: Long, request: AvatarUpdateRequest): AvatarResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }

        user.changeAvatar(
            newHead = request.head,
            newTop = request.top,
            newBottom = request.bottom,
            newShoes = request.shoes
        )

        return user.avatar.toResponse()
    }
}
