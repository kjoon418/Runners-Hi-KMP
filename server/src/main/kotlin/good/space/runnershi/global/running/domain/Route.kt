package good.space.runnershi.global.running.domain

import good.space.runnershi.model.domain.location.LocationModel
import good.space.runnershi.model.dto.running.LocationPoint
import good.space.runnershi.util.DistanceCalculator
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

@Entity
class Route (
    @ManyToOne(fetch = FetchType.LAZY)
    var running: Running? = null,

    @OneToMany(mappedBy = "route", cascade = [CascadeType.ALL], orphanRemoval = true)
    val points: MutableList<Point> = mutableListOf()
)
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    fun createPoints(locationDtos: List<LocationPoint>){
        locationDtos.forEach { dto ->
            val point = Point(
                latitude = dto.latitude,
                longitude = dto.longitude,
                sequenceOrder = dto.sequenceOrder,
                timestamp = dto.timestamp,
                segmentIndex = dto.segmentIndex
            )
            this.addPoint(point) // 아래의 편의 메서드 호출
        }
    }

    fun addPoint(point: Point) {
        points.add(point)
        point.route = this
    }
    fun calculateDistance(): Double {
        if (points.size < 2) return 0.0

        val sortedPoints = points.sortedBy { it.sequenceOrder }

        return sortedPoints.zipWithNext { p1, p2 ->
            DistanceCalculator.calculateDistance(
                LocationModel(
                    latitude = p1.latitude,
                    longitude = p1.longitude,
                    timestamp = p1.timestamp.toEpochMilliseconds() // Instant -> Long 변환
                ),
                LocationModel(
                    latitude = p2.latitude,
                    longitude = p2.longitude,
                    timestamp = p2.timestamp.toEpochMilliseconds() // Instant -> Long 변환
                )
            )
        }.sum()
    }
}
