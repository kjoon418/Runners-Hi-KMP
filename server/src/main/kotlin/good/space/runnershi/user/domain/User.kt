package good.space.runnershi.user.domain

import good.space.runnershi.global.running.converter.KotlinLocalDateConverter
import good.space.runnershi.model.dto.running.RunCreateRequest
import good.space.runnershi.user.UserType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinLocalDate
import java.time.ZoneId
import kotlin.time.DurationUnit

@Inheritance(strategy = InheritanceType.JOINED)
@Entity
@Table(name = "users")
abstract class User(
    var name: String,

    @Column(unique = true, nullable = false)
    var email: String,

    @Enumerated(EnumType.STRING)
    var userType: UserType,
){
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var exp: Long = 0;
    var totalRunningDays: Long = 0
    var totalDistanceMeters: Double = 300.0
    var totalRunningHours: Double = 0.0
    var bestPace: Double = 0.0
    var longestDistanceMeters: Double = 0.0
    var averagePace: Double = 0.0

    @Convert(converter = KotlinLocalDateConverter::class)
    var lastRunDate: LocalDate? = null

    @ElementCollection(targetClass = Achievement::class, fetch = FetchType.LAZY)
    @CollectionTable(
        name = "user_achievements",
        joinColumns = [JoinColumn(name = "user_id")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "achievement")
    val achievements: MutableSet<Achievement> = mutableSetOf()

    fun increaseExp(amount: Long) {
        this.exp += amount
    }

    fun updateRunningStats(request: RunCreateRequest) {
        val durationSeconds = request.runningDuration.toDouble(DurationUnit.SECONDS)
        val pace: Double = durationSeconds * (1000.0 / request.distanceMeters)

        this.totalDistanceMeters += request.distanceMeters.toLong()

        this.totalRunningHours += durationSeconds / 3600.0

        if (request.distanceMeters > this.longestDistanceMeters) {
            this.longestDistanceMeters = request.distanceMeters
        }


        if (this.bestPace == 0.0 || pace < this.bestPace) {
            this.bestPace = pace
        }

        if (totalDistanceMeters > 0) {
            val totalSeconds = totalRunningHours * 3600
            this.averagePace = totalSeconds * (1000.0 / totalDistanceMeters)
        }

        val currentRunDate = request.startedAt
            .toJavaInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toKotlinLocalDate()

        if (this.lastRunDate == null || this.lastRunDate != currentRunDate) {
            this.totalRunningDays += 1
            this.lastRunDate = currentRunDate
        }

        updateAchievement()
    }

    private fun updateAchievement() {
        for (achievement in Achievement.entries) {
            if (achievement.available(this)) {
                achievements.add(achievement)
            }
        }
    }
}
