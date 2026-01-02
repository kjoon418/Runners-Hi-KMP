package good.space.runnershi.model.dto.running

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class LocationPoint @OptIn(ExperimentalTime::class) constructor(
    val latitude: Double,
    val longitude: Double,
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant,
    val segmentIndex: Int, // [핵심] 끊어진 구간(Pause/Resume) 식별자
    val sequenceOrder: Int // [핵심] 점의 순서 (서버 정렬 보장용)
)
