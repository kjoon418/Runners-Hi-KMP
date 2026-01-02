package good.space.runnershi.model.dto.running

import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class RunningHistoryResponse @OptIn(ExperimentalTime::class) constructor(
    val runId: Long,

    @Serializable(with = InstantSerializer::class)
    val startedAt: Instant,
    val distanceMeters: Double,
    @Serializable(with = DurationSerializer::class)
    val durationSeconds: Duration,
    val averagePace: Double
)
