package good.space.runnershi.model.dto.running

import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toDuration

/**
 * Instant를 ISO-8601 문자열로 직렬화/역직렬화
 */
@OptIn(ExperimentalTime::class)
object InstantSerializer : kotlinx.serialization.KSerializer<Instant> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor(
        "Instant",
        kotlinx.serialization.descriptors.PrimitiveKind.STRING
    )

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}

/**
 * Duration을 초 단위 Long으로 직렬화/역직렬화
 */
object DurationSerializer : kotlinx.serialization.KSerializer<Duration> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor(
        "good.space.runnershi.Duration",
        kotlinx.serialization.descriptors.PrimitiveKind.LONG
    )

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: Duration) {
        encoder.encodeLong(value.inWholeSeconds)
    }

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): Duration {
        return decoder.decodeLong().toDuration(DurationUnit.SECONDS)
    }
}

@Serializable
data class RunCreateRequest @OptIn(ExperimentalTime::class) constructor(
    // [Header] 러닝 요약 정보
    val distanceMeters: Double,
    @Serializable(with = DurationSerializer::class)
    val runningDuration: Duration, // 실제 러닝 시간 (PAUSE 시간 제외)
    @Serializable(with = DurationSerializer::class)
    val totalDuration: Duration, // 휴식시간을 포함한 총 시간
    @Serializable(with = InstantSerializer::class)
    val startedAt: Instant, // 러닝 시작 시점
    // [Body] 경로 데이터 (RDB 저장을 위한 Flat List)
    val locations: List<LocationPoint>
)

