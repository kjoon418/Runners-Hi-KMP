package good.space.runnershi.global.running.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

@Entity
class Point (
    val latitude: Double, //위도
    val longitude: Double, //경도
    val sequenceOrder: Int,
    val timestamp: String,
    val segmentIndex: Int,
    @ManyToOne(fetch = FetchType.LAZY)
    var route: Route? = null
){
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}