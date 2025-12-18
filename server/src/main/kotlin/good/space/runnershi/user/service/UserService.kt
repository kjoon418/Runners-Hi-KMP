package good.space.runnershi.user.service

import good.space.runnershi.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService (
    private val userRepository: UserRepository
)
