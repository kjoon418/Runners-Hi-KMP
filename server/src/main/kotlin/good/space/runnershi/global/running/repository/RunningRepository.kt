package good.space.runnershi.global.running.repository

import good.space.runnershi.global.running.entity.Running
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RunningRepository : JpaRepository<Running, Long> {

}