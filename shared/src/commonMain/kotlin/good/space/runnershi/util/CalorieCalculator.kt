package good.space.runnershi.util

import kotlin.math.roundToInt

object CalorieCalculator {

    // 사용자의 체중 데이터가 없을 경우 사용할 기본값 (대한민국 성인 평균 근사치)
    private const val DEFAULT_WEIGHT_KG = 70.0

    /**
     * 러닝 칼로리 계산
     * 공식: 거리(km) x 체중(kg) x 1.036 (러닝 계수)
     * 
     * @param distanceMeters 이동 거리 (미터)
     * @param weightKg 사용자 체중 (kg) - 추후 프로필 기능 연동 시 입력받음
     * @return 소모 칼로리 (kcal)
     */
    fun calculateCalories(distanceMeters: Double, weightKg: Double = DEFAULT_WEIGHT_KG): Int {
        if (distanceMeters <= 0.0) return 0
        
        val distanceKm = distanceMeters / 1000.0
        
        // 러닝의 경우 통상적으로 1km당 체중 1kg당 1kcal가 소모된다고 봅니다.
        // 조금 더 정밀한 계수(1.036)를 적용하기도 합니다.
        val kcal = distanceKm * weightKg * 1.036
        
        return kcal.roundToInt()
    }
}

