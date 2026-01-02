package good.space.runnershi.model.domain.location

import kotlinx.serialization.Serializable

/**
 * 사용자의 위치 데이터를 담는다
 *
 * @property latitude 위도
 * @property longitude 경도
 * @property timestamp 시간
 * @property speed 속도 (m/s)
 * @property accuracy GPS 오차 범위(단위: meter)
 */
@Serializable
data class LocationModel(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val speed: Float = 0f,
    val accuracy: Float = 0f
)

