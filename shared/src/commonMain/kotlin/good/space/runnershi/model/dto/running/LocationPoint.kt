package good.space.runnershi.model.dto.running

import kotlinx.serialization.Serializable

@Serializable
data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: String, // ISO-8601 형식
    val segmentIndex: Int, // [핵심] 끊어진 구간(Pause/Resume) 식별자
    val sequenceOrder: Int // [핵심] 점의 순서 (서버 정렬 보장용)
)
