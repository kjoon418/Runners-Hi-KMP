package good.space.runnershi.global.running.entity

import good.space.runnershi.model.dto.running.LocationPoint
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
}
