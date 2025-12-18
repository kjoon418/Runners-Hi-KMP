package good.space.runnershi.global.running.service

import good.space.runnershi.global.running.entity.Running
import good.space.runnershi.global.running.repository.RunningRepository
import good.space.runnershi.model.dto.running.RunCreateRequest
import good.space.runnershi.model.dto.running.UpdatedUserResponse
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

        val runId: Long = saveRunningData(user, runCreateRequest)
        updateUserByRunnigData(user, runCreateRequest)

        return user.toUpdatedUserResponse(runId)
    }

    private fun saveRunningData(user: User, request: RunCreateRequest): Long {
        val running = Running(
            duration = request.runningDuration, // Duration 타입
            totalTime = request.totalDuration, // Duration 타입
            distanceMeters = request.distanceMeters,
            startedAt = request.startedAt, // Instant 타입
            user = user
        )

        running.createRoute(request.locations)
        val savedRunning = runningRepository.save(running)

        return savedRunning.id!!
    }

    private fun updateUserByRunnigData(user: User, request: RunCreateRequest): Unit {
        user.increaseExp(request.distanceMeters.toLong());
        user.updateRunningStats(request)
    }

    private fun User.toUpdatedUserResponse(runId: Long): UpdatedUserResponse {
        return UpdatedUserResponse(
            runId = runId,
            userId = this.id ?: throw IllegalStateException("ID가 없는 유저입니다."),
            userExp = this.exp,
            totalRunningDays = this.totalRunningDays,
            badges = this.achievements.map { it.name }
        )
    }
}
