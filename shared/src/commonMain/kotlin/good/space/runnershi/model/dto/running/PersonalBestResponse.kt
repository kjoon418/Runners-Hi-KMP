package good.space.runnershi.model.dto.running

import kotlinx.serialization.Serializable

@Serializable
data class PersonalBestResponse(
    val distanceMeters: Double,   // 예: 10500.0 (10.5km)
    val durationSeconds: Long,    // 예: 3600 (1시간)
    val startedAt: String         // 예: "2024-12-25T08:00:00Z"
)

