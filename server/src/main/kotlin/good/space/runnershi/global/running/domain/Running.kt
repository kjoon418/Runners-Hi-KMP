package good.space.runnershi.global.running.domain

import good.space.runnershi.model.dto.running.LocationPoint
import good.space.runnershi.user.domain.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Transient
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import good.space.runnershi.global.running.converter.KotlinInstantConverter
import jakarta.persistence.Convert
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Entity
@OptIn(ExperimentalTime::class)
class Running (
    @Column(nullable = false)
    val durationMillis: Long, // 실제 러닝 시간 (PAUSE 시간 제외) - 밀리초

    @Column(nullable = false)
    val totalTimeMillis: Long, // 휴식시간을 포함한 총 시간 - 밀리초

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


    // Duration 타입의 computed property (기존 코드 호환성)
    // 데이터베이스에 저장되지 않도록 @Transient 추가
    @get:Transient
    val duration: Duration
        get() = durationMillis.toDuration(DurationUnit.MILLISECONDS)
    
    @get:Transient
    val totalTime: Duration
        get() = totalTimeMillis.toDuration(DurationUnit.MILLISECONDS)

    @get:Transient
    val averagePace: Double = if (distanceMeters > 0 && durationMillis > 0) {
        val durationSeconds = durationMillis / 1000.0
        1000 / (distanceMeters / durationSeconds)
    } else {
        0.0
    }
}
