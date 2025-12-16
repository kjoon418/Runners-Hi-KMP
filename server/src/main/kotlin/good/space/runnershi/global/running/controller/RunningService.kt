package good.space.runnershi.global.running.controller

import good.space.runnershi.global.running.entity.Running
import good.space.runnershi.global.running.repository.RunningRepository
import good.space.runnershi.model.dto.running.RunCreateRequest
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
            durationSeconds = request.durationSeconds,
            distanceMeters = request.distanceMeters,
            stratedAt = request.startedAt,
            user = user
        )

        running.createRoute(request.locations)
        val savedRunning = runningRepository.save(running)

        return savedRunning.id!!
    }
}
