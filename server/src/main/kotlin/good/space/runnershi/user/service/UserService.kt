package good.space.runnershi.user.service

import good.space.runnershi.model.dto.running.HomeQuestInfo
import good.space.runnershi.model.dto.running.UserHomeResponse
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
            achievements = user.achievements.map { it.name }
        )
    }
}
