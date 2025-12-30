package good.space.runnershi.repository

import good.space.runnershi.model.dto.user.QuestResponse
import good.space.runnershi.network.ApiClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode

class QuestRepositoryImpl(
    private val apiClient: ApiClient
) : QuestRepository {
    
    override suspend fun getQuestData(): Result<List<QuestResponse>> {
        return try {
            val response = apiClient.httpClient.get("${apiClient.baseUrl}/api/quest/quest")
            
            if (response.status == HttpStatusCode.OK) {
                val questList = response.body<List<QuestResponse>>()
                Result.success(questList)
            } else {
                Result.failure(Exception("퀘스트 데이터 조회 실패: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

