package good.space.runnershi.global.running.repository

import good.space.runnershi.global.running.domain.Running
import kotlinx.datetime.Instant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RunningRepository : JpaRepository<Running, Long> {
    fun findAllByUserIdAndStartedAtBetween(
        userId: Long,
        start: Instant,
        end: Instant
    ): List<Running>
}
