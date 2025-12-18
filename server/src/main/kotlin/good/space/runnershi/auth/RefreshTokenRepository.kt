package good.space.runnershi.auth

import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByUserId(userId: Long): RefreshToken?
    fun deleteByUserId(userId: Long)
}
