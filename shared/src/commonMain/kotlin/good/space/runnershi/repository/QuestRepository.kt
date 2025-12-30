package good.space.runnershi.repository

import good.space.runnershi.model.dto.user.QuestResponse

interface QuestRepository {
    suspend fun getQuestData(): Result<List<QuestResponse>>
}
