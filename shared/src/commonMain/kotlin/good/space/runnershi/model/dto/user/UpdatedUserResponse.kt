package good.space.runnershi.model.dto.running

import kotlinx.serialization.Serializable

@Serializable // KMP 환경이라면 직렬화 필요
data class UpdatedUserResponse(
    val runId: Long,
    val userId: Long,
    val userExp: Long,
    val totalRunningDays: Long,
    val badges: List<String> // Achievement 객체 대신 String 리스트로 받습니다.
)
