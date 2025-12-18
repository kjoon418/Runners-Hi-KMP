package good.space.runnershi.user.controller

import good.space.runnershi.user.service.UserService
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController (
    private var userService: UserService


    )
