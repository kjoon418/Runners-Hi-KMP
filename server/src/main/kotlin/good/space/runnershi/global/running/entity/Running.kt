package good.space.runnershi.global.running.entity

import good.space.runnershi.model.dto.running.LocationPoint
import good.space.runnershi.user.domain.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

@Entity
class Running (
    val durationSeconds: Long,
    val distanceMeters: Double,
    val stratedAt: String,

    @ManyToOne
    var user: User? = null

){
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @OneToMany(mappedBy = "running", cascade = [CascadeType.ALL], orphanRemoval = true)
    val routes: MutableList<Route> = mutableListOf()

    fun createRoute(locations: List<LocationPoint>) {
        val groupedLocations = locations.groupBy { it.segmentIndex }

        groupedLocations.forEach { (index, pointsInSegment) ->
            val newRoute = Route()
            newRoute.createPoints(pointsInSegment)
            this.addRoute(newRoute)
        }
    }

    fun addRoute(route: Route) {
        routes.add(route)
        route.running = this
    }

    val averagePace: Double = if (distanceMeters > 0 && durationSeconds > 0) {
        1000 / (distanceMeters / durationSeconds.toDouble())
    } else {
        0.0
    }
}
