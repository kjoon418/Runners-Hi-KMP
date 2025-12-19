package good.space.runnershi.model.domain

import good.space.runnershi.model.domain.location.LocationModel
import good.space.runnershi.model.domain.running.PaceCalculator
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

data class RunResult(
    val id: String? = null, // 서버에서 발급받을 ID
    val totalDistanceMeters: Double,
    val duration: Duration, // 실제 러닝 시간 (PAUSE 시간 제외)
    val totalTime: Duration, // 휴식시간을 포함한 총 시간 (시작부터 종료까지)
    val pathSegments: List<List<LocationModel>>, // 경로 데이터
    val calories: Int, // (거리 * 몸무게 * 계수)로 추후 계산
    val startedAt: Instant = Clock.System.now() // 러닝 시작 시점
) {
    // 1. 쉬는 시간 미포함 페이스 (Moving Pace) - 실제 달리기 능력
    val movingPace: String
        get() = PaceCalculator.calculatePace(totalDistanceMeters, duration.inWholeSeconds)

    // 2. 쉬는 시간 포함 페이스 (Elapsed Pace) - 전체 훈련 강도
    val elapsedPace: String
        get() = PaceCalculator.calculatePace(totalDistanceMeters, totalTime.inWholeSeconds)
    
    // 하위 호환성을 위한 avgPace (movingPace와 동일)
    @Deprecated("movingPace를 사용하세요", ReplaceWith("movingPace"))
    val avgPace: String
        get() = movingPace
}

