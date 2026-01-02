package good.space.runnershi.repository

import good.space.runnershi.mapper.RunMapper
import good.space.runnershi.model.domain.RunResult
import good.space.runnershi.model.dto.running.LongestDistance
import good.space.runnershi.model.dto.user.UpdatedUserResponse
import good.space.runnershi.model.dto.running.percentile.RunPercentileRequest
import good.space.runnershi.model.dto.running.percentile.RunPercentileResponse
import good.space.runnershi.network.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class RunningRepositoryImpl(
    private val apiClient: ApiClient
) : RunningRepository {
    
    override suspend fun saveRun(runResult: RunResult): Result<UpdatedUserResponse> {
        return try {
            // 1. Domain Model -> DTO 변환
            val request = RunMapper.mapToCreateRequest(runResult)
            
            // 2. API 호출 (인증은 ApiClient의 httpClient가 자동 처리)
            val response = apiClient.httpClient.post("${apiClient.baseUrl}/api/v1/running/run-records") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            if (response.status == HttpStatusCode.OK) {
                val updatedUserResponse = response.body<UpdatedUserResponse>()
                Result.success(updatedUserResponse)
            } else {
                Result.failure(Exception("러닝 기록 업로드 실패: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getPersonalBest(): Result<LongestDistance?> {
        return try {
            // GET /api/v1/running/LongestDistance
            val response = apiClient.httpClient.get("${apiClient.baseUrl}/api/v1/running/LongestDistance")

            when (response.status) {
                HttpStatusCode.OK -> {
                    val longestDistance = response.body<LongestDistance>()
                    Result.success(longestDistance)
                }
                HttpStatusCode.NotFound -> {
                    // 아직 기록이 없는 경우
                    Result.success(null)
                }
                else -> {
                    Result.failure(Exception("최대 거리 조회 실패: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getPercentile(distanceMeters: Double, durationSeconds: Long): Result<RunPercentileResponse> {
        return try {
            val request = RunPercentileRequest(
                totalDistanceMeters = distanceMeters,
                durationSec = durationSeconds.toInt()
            )
            
            val response = apiClient.httpClient.post("${apiClient.baseUrl}/api/v1/running/percentile") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            if (response.status == HttpStatusCode.OK) {
                val percentileResponse = response.body<RunPercentileResponse>()
                Result.success(percentileResponse)
            } else {
                Result.failure(Exception("퍼센타일 조회 실패: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

