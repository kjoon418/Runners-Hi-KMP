package good.space.runnershi.global.running.entity

import good.space.runnershi.model.dto.running.LocationPoint
import good.space.runnershi.user.domain.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import kotlinx.datetime.Instant
import kotlin.time.Duration
import good.space.runnershi.global.running.converter.KotlinDurationConverter
import good.space.runnershi.global.running.converter.KotlinInstantConverter
import good.space.runnershi.user.domain.Quest

@Entity
class Running (
    @Convert(converter = KotlinDurationConverter::class)
    @Column(nullable = false)
    val duration: Duration, // 실제 러닝 시간 (PAUSE 시간 제외)
    
    @Convert(converter = KotlinDurationConverter::class)
    @Column(nullable = false)
    val totalTime: Duration, // 휴식시간을 포함한 총 시간

    val distanceMeters: Double,

    @Column(nullable = false)
    var longestNonStopDistance: Double = 0.0,

    @Convert(converter = KotlinInstantConverter::class)
    @Column(nullable = false)
    val startedAt: Instant, // 러닝 시작 시점

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

    fun updateLongestNonStopDistance() {
        this.longestNonStopDistance = routes
            .maxOfOrNull { route -> route.calculateDistance() }
            ?: 0.0
    }


    val averagePace: Double = if (distanceMeters > 0 && duration.inWholeSeconds > 0) {
        1000 / (distanceMeters / duration.inWholeSeconds.toDouble())
    } else {
        0.0
    }


}
