package good.space.runnershi.repository

import good.space.runnershi.model.domain.RunResult
import good.space.runnershi.model.dto.running.LongestDistance
import good.space.runnershi.model.dto.user.UpdatedUserResponse
import good.space.runnershi.model.dto.running.percentile.RunPercentileResponse

interface RunningRepository {
    // 러닝 기록을 서버에 전송하는 함수
    suspend fun saveRun(runResult: RunResult): Result<UpdatedUserResponse>
    
    // 최대 거리 조회
    suspend fun getPersonalBest(): Result<LongestDistance?>
    
    // 페이스 퍼센타일 조회
    suspend fun getPercentile(distanceMeters: Double, durationSeconds: Long): Result<RunPercentileResponse>
}

