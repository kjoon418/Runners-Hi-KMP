package good.space.runnershi.user.controller

import good.space.runnershi.model.dto.user.AvatarResponse
import good.space.runnershi.model.dto.user.AvatarUpdateRequest
import good.space.runnershi.model.dto.user.QuestResponse
import good.space.runnershi.model.dto.user.UserHomeResponse
import good.space.runnershi.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/home")
    fun getHomeData(
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<UserHomeResponse> {
        val response = userService.loadHomeData(userId)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "아바타 변경", description = "유저의 아바타(아이템)를 변경합니다. 해제하려면 NONE을 보내세요.")
    @PutMapping("/avatar")
    fun updateAvatar(
        @AuthenticationPrincipal userId: Long,
        @RequestBody request: AvatarUpdateRequest
    ): ResponseEntity<AvatarResponse> {
        val response = userService.changeAvatar(userId, request)

        return ResponseEntity.ok(response)
    }
}
