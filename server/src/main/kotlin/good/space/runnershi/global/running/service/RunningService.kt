package good.space.runnershi.global.running.service

import good.space.runnershi.global.running.entity.Running
import good.space.runnershi.global.running.repository.RunningRepository
import good.space.runnershi.model.dto.running.RunCreateRequest
import good.space.runnershi.model.dto.user.UpdatedUserResponse
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
    fun createRunning(userId: Long, request: RunCreateRequest): Long {
        val user = userRepository.findById(userId)
            .orElseThrow{ IllegalArgumentException("User를 찾을 수 없습니다 in RunningService") }

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

    @Transactional
    fun updateUserByRunnig(userId: Long, request: RunCreateRequest): Unit {
        val user = userRepository.findById(userId)
            .orElseThrow{ IllegalArgumentException("User를 찾을 수 없습니다 in RunningService") }

        user.increaseExp(request.distanceMeters.toLong());

        user.updateRunningStats(request)
    }

    private fun User.toUpdatedResponse(): UpdatedUserResponse {
        return UpdatedUserResponse(
            userId = this.id ?: throw IllegalStateException("ID가 없는 유저입니다."),
            exp = this.exp
        )
    }
}
