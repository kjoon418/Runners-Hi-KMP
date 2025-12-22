package good.space.runnershi.global.running.service

import good.space.runnershi.global.running.entity.Running
import good.space.runnershi.global.running.mapper.toLongestDistanceDto
import good.space.runnershi.global.running.repository.RunningRepository
import good.space.runnershi.model.dto.running.LongestDistance
import good.space.runnershi.model.dto.running.RunCreateRequest
import good.space.runnershi.model.dto.running.UpdatedUserResponse
import good.space.runnershi.model.dto.running.dailyQuestInfo
import good.space.runnershi.model.dto.running.newBadgeInfo
import good.space.runnershi.user.domain.User
import good.space.runnershi.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class RunningService (
    private val userRepository: UserRepository,
    private val runningRepository: RunningRepository
){
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
        user.addRunning(running)

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
