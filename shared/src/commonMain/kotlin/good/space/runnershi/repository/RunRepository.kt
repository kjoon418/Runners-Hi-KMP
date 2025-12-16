package good.space.runnershi.repository

import good.space.runnershi.model.domain.RunResult
import good.space.runnershi.model.dto.running.PersonalBestResponse

interface RunRepository {
    // 러닝 기록을 서버에 전송하는 함수
    suspend fun saveRun(runResult: RunResult): Result<String>
    
    // [NEW] 최대 기록 조회
    suspend fun getPersonalBest(): Result<PersonalBestResponse?>
}

