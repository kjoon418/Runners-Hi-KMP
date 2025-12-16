package good.space.runnershi.model.dto.running

import kotlinx.serialization.Serializable

@Serializable
data class RunCreateRequest(
    // [Header] 러닝 요약 정보
    val distanceMeters: Double,
    val durationSeconds: Long,
    val startedAt: String, // ISO-8601 형식 (예: "2024-05-20T07:00:00Z")
    // [Body] 경로 데이터 (RDB 저장을 위한 Flat List)
    val locations: List<LocationPoint>
)

