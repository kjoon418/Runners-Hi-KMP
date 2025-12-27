package good.space.runnershi.model.dto.running

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class RunningHistoryResponse(
    val runId: Long,

    @Serializable(with = InstantSerializer::class)
    val startedAt: Instant,
    val distanceMeters: Double,
    val durationSeconds: Duration,
    val averagePace: Double
)
