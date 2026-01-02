package good.space.runnershi.user.domain

import good.space.runnershi.global.running.converter.KotlinLocalDateConverter
import good.space.runnershi.global.running.domain.Running
import good.space.runnershi.model.domain.auth.Sex
import good.space.runnershi.model.domain.auth.UserType
import good.space.runnershi.model.dto.user.UnlockedItem
import good.space.runnershi.model.type.item.BottomItem
import good.space.runnershi.model.type.item.HeadItem
import good.space.runnershi.model.type.item.ShoeItem
import good.space.runnershi.model.type.item.TopItem
import good.space.runnershi.state.LevelPolicy
import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
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
import kotlinx.datetime.toKotlinLocalDate
import java.time.ZoneId
import kotlin.collections.mutableSetOf
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaInstant
import jakarta.persistence.Transient

@Inheritance(strategy = InheritanceType.JOINED)
@Entity
@Table(name = "users")
@OptIn(ExperimentalTime::class)
abstract class User(
    @Column(unique = true, nullable = false)
    var name: String,

    @Column(unique = true, nullable = false)
    var email: String,

    @Enumerated(EnumType.STRING)
    var userType: UserType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var sex: Sex
){
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var exp: Long = 0;
    var totalRunningDays: Long = 0
    var totalDistanceMeters: Double = 0.0
    var totalRunningHours: Double = 0.0
    var bestPace: Double = 0.0
    var longestDistanceMeters: Double = 0.0
    var averagePace: Double = 0.0

    @OneToMany(cascade = [(CascadeType.ALL)], orphanRemoval = true)
    var runnings: MutableList<Running> = mutableListOf()

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

    @Embedded
    var avatar: Avatar = Avatar()

    @Embedded
    var inventory: UserInventory = UserInventory()

    @Transient
    private var _newAchievements: MutableSet<Achievement>? = null
    val newAchievements: MutableSet<Achievement>
        @Transient get() {
            if (_newAchievements == null) {
                _newAchievements = mutableSetOf()
            }
            return _newAchievements!!
        }

    @Transient
    private var _newUnlockedAvatars: MutableList<UnlockedItem>? = null
    val newUnlockedAvatars: MutableList<UnlockedItem>
        @Transient get() {
            if (_newUnlockedAvatars == null) {
                _newUnlockedAvatars = mutableListOf()
            }
            return _newUnlockedAvatars!!
        }

    @Transient
    private var _newCompletedQuests: MutableList<Quest>? = null
    val newCompletedQuests: MutableList<Quest>
        @Transient get() {
            if (_newCompletedQuests == null) {
                _newCompletedQuests = mutableListOf()
            }
            return _newCompletedQuests!!
        }

    var level: Int = 1

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
        checkLevelUp()
    }

    private fun checkLevelUp() {
        val calculatedLevel = LevelPolicy.calculateLevel(this.exp)

        if (calculatedLevel > this.level) {
            this.level = calculatedLevel
            updateAvatars()
        }
    }

    fun updateRunningStats(running: Running) {
        val durationSeconds = running.duration.toDouble(DurationUnit.SECONDS)
        
        this.totalDistanceMeters += running.distanceMeters.toLong()

        this.totalRunningHours += durationSeconds / 3600.0

        if (running.distanceMeters > this.longestDistanceMeters) {
            this.longestDistanceMeters = running.distanceMeters
        }

        // pace 계산: distanceMeters가 0이면 Infinity가 되므로 체크 필요
        if (running.distanceMeters > 0) {
            val pace: Double = durationSeconds * (1000.0 / running.distanceMeters)
            if (this.bestPace == 0.0 || pace < this.bestPace) {
                this.bestPace = pace
            }
        }

        // averagePace 계산: totalDistanceMeters가 0이면 Infinity가 되므로 체크 필요
        if (totalDistanceMeters > 0 && totalRunningHours > 0) {
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

    private fun updateAvatars(){
        HeadItem.entries.forEach { item ->
            if (item.requiredLevel ==  this.level && !inventory.hasHead(item)) {
                newUnlockedAvatars.add(UnlockedItem(item))
                inventory.addHead(item)
            }
        }

        TopItem.entries.forEach { item ->
            if (item.requiredLevel ==  this.level && !inventory.hasTop(item)) {
                newUnlockedAvatars.add(UnlockedItem(item))
                inventory.addTop(item)
            }
        }

        BottomItem.entries.forEach { item ->
            if (item.requiredLevel ==  this.level && !inventory.hasBottom(item)) {
                newUnlockedAvatars.add(UnlockedItem(item))
                inventory.addBottom(item)
            }
        }

        ShoeItem.entries.forEach { item ->
            if (item.requiredLevel ==  this.level && !inventory.hasShoe(item)) {
                newUnlockedAvatars.add(UnlockedItem(item))
                inventory.addShoe(item)
            }
        }
    }

    fun changeAvatar(newHead: HeadItem, newTop: TopItem, newBottom: BottomItem, newShoes: ShoeItem) {
        this.avatar.head = newHead
        this.avatar.top = newTop
        this.avatar.bottom = newBottom
        this.avatar.shoes = newShoes
    }

    private fun checkDailyQuests(running: Running) {
        for (status in this.dailyQuests) {
            if (status.isCompleted) continue

            if (status.quest.available(running)) {
                status.isCompleted = true

                this.increaseExp(status.quest.exp)
                this.newCompletedQuests.add(status.quest)
            }
        }
    }

    private fun updateAchievements() {
        for (achievement in Achievement.entries) {
            if (achievement.available(this) && !this.achievements.contains(achievement)) {
                achievements.add(achievement)
                newAchievements.add(achievement)
                this.increaseExp(achievement.exp)
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
