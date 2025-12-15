package good.space.runnershi.util

import good.space.runnershi.model.domain.LocationModel
import kotlin.math.*

object DistanceCalculator {
    private const val EARTH_RADIUS_METERS = 6371000.0 // 지구 반지름 (미터)

    /**
     * 두 지점 간의 거리를 미터(m) 단위로 반환합니다.
     */
    fun calculateDistance(start: LocationModel, end: LocationModel): Double {
        val lat1 = toRadians(start.latitude)
        val lon1 = toRadians(start.longitude)
        val lat2 = toRadians(end.latitude)
        val lon2 = toRadians(end.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2) +
                cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        
        val c = 2 * asin(sqrt(a))

        return EARTH_RADIUS_METERS * c
    }

    private fun toRadians(deg: Double): Double {
        return deg * (PI / 180)
    }
}

