package good.space.runnershi.global.running.service

import good.space.runnershi.global.running.domain.Running
import good.space.runnershi.global.running.mapper.toLongestDistanceDto
import good.space.runnershi.global.running.repository.RunningRepository
import good.space.runnershi.model.dto.running.LongestDistance
import good.space.runnershi.model.dto.running.RunCreateRequest
import good.space.runnershi.model.dto.running.RunningHistoryResponse
import good.space.runnershi.model.dto.running.UpdatedUserResponse
import good.space.runnershi.model.dto.running.dailyQuestInfo
import good.space.runnershi.model.dto.running.newBadgeInfo
import good.space.runnershi.user.domain.User
import good.space.runnershi.user.repository.UserRepository
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RunningService (
    private val userRepository: UserRepository,
    private val runningRepository: RunningRepository
){
    @Transactional(readOnly = true)
    fun getRunningHistory(userId: Long, startDate: LocalDate, endDate: LocalDate): List<RunningHistoryResponse> {
        val timeZone = TimeZone.currentSystemDefault()

        val startInstant = startDate.atStartOfDayIn(timeZone)

        val endInstant = endDate.plus(1, DateTimeUnit.DAY)
            .atStartOfDayIn(timeZone)

        val runnings = runningRepository.findAllByUserIdAndStartedAtBetween(
            userId, startInstant, endInstant
        )

        return runnings.map { running ->
            RunningHistoryResponse(
                runId = running.id!!,
                distanceMeters = running.distanceMeters,
                durationSeconds = running.duration,
                startedAt = running.startedAt,
                averagePace = running.averagePace
            )
        }
    }

    @Transactional
    fun saveRunningStats(userId: Long, runCreateRequest: RunCreateRequest): UpdatedUserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow{ IllegalArgumentException("user with id $userId 를 찾을 수 없습니다. in RunningService") }

        val savedRunning = saveRunningData(user, runCreateRequest)
        updateUserByRunnigData(user, savedRunning)

        return user.toUpdatedUserResponse(savedRunning)
    }

    private fun saveRunningData(user: User, request: RunCreateRequest): Running{
        val running = Running(
            duration = request.runningDuration,
            totalTime = request.totalDuration,
            distanceMeters = request.distanceMeters,
            startedAt = request.startedAt,
            longestNonStopDistance = 0.0,
            user = user
        )

        running.createRoute(request.locations)
        running.updateLongestNonStopDistance()

        val savedRunning = runningRepository.save(running)

        return savedRunning
    }

    private fun updateUserByRunnigData(user: User, running: Running): Unit {
        user.increaseExp(running.distanceMeters.toLong());
        user.updateRunningStats(running)
    }


    fun getLongestDistance(userId: Long): LongestDistance {
        val user: User = userRepository.findById(userId)
            .orElseThrow{ IllegalArgumentException("user with id $userId 를 찾을 수 없습니다. in RunningService") }

        return user.toLongestDistanceDto()
    }

    private fun User.toUpdatedUserResponse(running: Running): UpdatedUserResponse {
        return UpdatedUserResponse(
            userId = this.id ?: throw IllegalStateException("ID가 없는 유저입니다."),
            userExp = this.exp,
            totalRunningDays = this.totalRunningDays,
            badges = this.achievements.map { it.name },
            newBadges = this.newAchievements.map {
                newBadgeInfo(
                    name = it.name,
                    exp = it.exp
                )
            },
            dailyQuests = this.dailyQuests.map { status ->
                dailyQuestInfo(
                    title = status.quest.title,
                    exp = status.quest.exp,
                    isComplete = status.isCompleted
                )
            }
        )
    }
}
