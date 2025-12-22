package good.space.runnershi.user.domain

import good.space.runnershi.global.running.converter.KotlinLocalDateConverter
import good.space.runnershi.global.running.entity.Running
import good.space.runnershi.model.dto.running.RunCreateRequest
import good.space.runnershi.user.UserType
import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
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
import jakarta.persistence.OneToMany
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

    @OneToMany(cascade = [(CascadeType.ALL)], orphanRemoval = true)
    var runnings: MutableList<Running> = mutableListOf()

    @Transient
    val newAchievements : MutableSet<Achievement> = mutableSetOf()

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

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "user_daily_quests",
        joinColumns = [JoinColumn(name = "user_id")]
    )
    @Enumerated(EnumType.STRING)
    var dailyQuests: MutableList<DailyQuestStatus> = mutableListOf()

    @Convert(converter = KotlinLocalDateConverter::class)
    var questGeneratedDate: LocalDate? = null

    fun refreshDailyQuestsIfNeeded() {
        val today = java.time.LocalDate.now().toKotlinLocalDate()

        if (this.questGeneratedDate == today && this.dailyQuests.isNotEmpty()) {
            return
        }

        this.dailyQuests.clear()

        this.dailyQuests.add(DailyQuestStatus(Quest.getRandomQuestByLevel(1), false))
        this.dailyQuests.add(DailyQuestStatus(Quest.getRandomQuestByLevel(2), false))
        this.dailyQuests.add(DailyQuestStatus(Quest.getRandomQuestByLevel(3), false))

        this.questGeneratedDate = today
    }

    fun addRunning(running: Running) {
        this.runnings.add(running)
        running.user = this
    }

    fun increaseExp(amount: Long) {
        this.exp += amount
    }

    fun updateRunningStats(running: Running) {
        val durationSeconds = running.duration.toDouble(DurationUnit.SECONDS)
        val pace: Double = durationSeconds * (1000.0 / running.distanceMeters)

        this.totalDistanceMeters += running.distanceMeters.toLong()

        this.totalRunningHours += durationSeconds / 3600.0

        if (running.distanceMeters > this.longestDistanceMeters) {
            this.longestDistanceMeters = running.distanceMeters
        }

        if (this.bestPace == 0.0 || pace < this.bestPace) {
            this.bestPace = pace
        }

        if (totalDistanceMeters > 0) {
            val totalSeconds = totalRunningHours * 3600
            this.averagePace = totalSeconds * (1000.0 / totalDistanceMeters)
        }

        val currentRunDate = running.startedAt
            .toJavaInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toKotlinLocalDate()

        if (this.lastRunDate == null || this.lastRunDate != currentRunDate) {
            this.totalRunningDays += 1
            this.lastRunDate = currentRunDate
        }

        updateAchievements()
        checkDailyQuests(running)
    }

    private fun checkDailyQuests(running: Running) {
        for (status in this.dailyQuests) {
            if (status.isCompleted) continue

            if (status.quest.available(running)) {
                status.isCompleted = true

                this.increaseExp(status.quest.exp)
            }
        }
    }

    private fun updateAchievements() {
        for (achievement in Achievement.entries) {
            if (achievement.available(this) && !this.achievements.contains(achievement)) {
                achievements.add(achievement)
                newAchievements.add(achievement)
            }
        }
    }

    @Embeddable
    class DailyQuestStatus(
        @Enumerated(EnumType.STRING)
        val quest: Quest,
        var isCompleted: Boolean = false
    )
}
