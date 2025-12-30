package good.space.runnershi.quest.service

import good.space.runnershi.model.dto.user.QuestResponse
import good.space.runnershi.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class QuestService(
    private val userRepository: UserRepository
) {

    @Transactional
    fun getDailyQuests(userId: Long): List<QuestResponse> {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        user.refreshDailyQuestsIfNeeded()

        return user.dailyQuests.map { status ->
            QuestResponse(
                title = status.quest.title,
                exp = status.quest.exp,
                isCompleted = status.isCompleted
            )
        }
    }
}
