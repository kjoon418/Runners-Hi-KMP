package good.space.runnershi.global.running.domain

import good.space.runnershi.global.running.converter.KotlinInstantConverter
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import kotlinx.datetime.Instant

@Entity
@Table(name = "route_points")
class Point (
    val latitude: Double, //위도
    val longitude: Double, //경도
    val sequenceOrder: Int,

    @Convert(converter = KotlinInstantConverter::class)
    val timestamp: Instant,
    val segmentIndex: Int,
    @ManyToOne(fetch = FetchType.LAZY)
    var route: Route? = null
){
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}
