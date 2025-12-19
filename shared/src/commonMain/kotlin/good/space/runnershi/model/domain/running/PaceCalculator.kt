package good.space.runnershi.model.domain.running

import good.space.runnershi.util.format

object PaceCalculator {

    /**
     * 평균 페이스 계산
     * @param distanceMeters 이동 거리 (미터)
     * @param durationSeconds 경과 시간 (초)
     * @return "MM'SS''" 형식의 문자열 (예: 05'30'')
     */
    fun calculatePace(distanceMeters: Double, durationSeconds: Long): String {
        // 1. 거리가 너무 짧거나 0일 경우 처리
        if (distanceMeters <= 0.0) {
            return "-'--''"
        }

        // 2. km 단위로 변환
        val distanceKm = distanceMeters / 1000.0

        // 3. 1km당 걸리는 시간(초) 계산
        val paceSecondsPerKm = durationSeconds / distanceKm

        // 4. 비정상적인 값 필터링 (예: 걷기보다 느린 경우, 1km에 60분 이상)
        // 러닝 앱에서는 보통 30분~60분/km 이상이면 표시를 안 하거나 걷기로 간주
        if (paceSecondsPerKm >= 3600) { 
            return "-'--''"
        }

        // 5. 분/초 분리
        val minutes = (paceSecondsPerKm / 60).toInt()
        val seconds = (paceSecondsPerKm % 60).toInt()

        // 6. 포맷팅 (05'30'') - StringFormatter 활용
        return "%02d'%02d''".format(minutes, seconds)
    }
}
