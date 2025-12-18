package good.space.runnershi.auth

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/test")
class TestController {
    @GetMapping
    fun testPage(): String {
        return "🏃‍♂️ 인증 성공! 당신은 로그인된 유저입니다."
    }
}
