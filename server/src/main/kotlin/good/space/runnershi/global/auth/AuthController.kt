package good.space.runnershi.global.auth

import good.space.runnershi.model.dto.auth.LoginRequest
import good.space.runnershi.model.dto.auth.SignUpRequest
import good.space.runnershi.model.dto.auth.TokenRefreshRequest
import good.space.runnershi.model.dto.auth.TokenRefreshResponse
import good.space.runnershi.model.dto.auth.TokenResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    // 회원가입 API
    @PostMapping("/signup")
    fun signUp(@RequestBody request: SignUpRequest): ResponseEntity<String> {
        authService.signUp(request)
        return ResponseEntity
            .status(HttpStatus.CREATED) // 201 Created
            .body("회원가입 성공!")
    }

    // 로그인 API
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        val token = authService.login(request)
        return ResponseEntity.ok(token) // 200 OK + 토큰 반환
    }

    // 토큰 갱신 API
    // RequestBody로 refreshToken 문자열 하나만 받거나, DTO를 만들어 받아도 됩니다.
    @PostMapping("/refresh")
    fun refresh(@RequestBody request: TokenRefreshRequest): ResponseEntity<TokenRefreshResponse> {
        val token = authService.refreshAccessToken(request.refreshToken)
        return ResponseEntity.ok(token)
    }

    @PostMapping("/login/web")
    fun loginWeb(@RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        val originalToken = authService.login(request)

        val cookie = createRefreshTokenCookie(originalToken.refreshToken ?: "")

        val secureTokenResponse = originalToken.copy(refreshToken = null)

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString()) // ⭐️ 헤더에 쿠키 심기
            .body(secureTokenResponse) // Body에는 AccessToken만 있음
    }

    @PostMapping("/refresh/web")
    fun refreshWeb( @CookieValue("refreshToken") refreshToken: String
    ): ResponseEntity<TokenRefreshResponse> {
        val newTokenResponse = authService.refreshAccessToken(refreshToken)

        val cookie = createRefreshTokenCookie(newTokenResponse.refreshToken)

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString()) // ⭐️ 갱신된 쿠키 다시 심기
            .body(newTokenResponse)
    }

    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<String> {

        authService.logout(userId)

        return ResponseEntity.ok("로그아웃 되었습니다.")
    }

    @PostMapping("/logout/web")
    fun logoutWeb(
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<String> {

        authService.logout(userId)
        val cookie = createRefreshTokenCookie(null)

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString()) // 쿠키 삭제 명령 전달
            .body("로그아웃 되었습니다.")
    }


    private fun createRefreshTokenCookie(refreshToken: String?): ResponseCookie {
        if (refreshToken.isNullOrBlank()) {
            return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0) //핵심: 수명을 0으로 설정하면 브라우저가 즉시 삭제함
                .sameSite("Strict")
                .build()
        }

        return ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)    // ⭐️ JS 접근 불가 (XSS 방지)
            .secure(false)     // ⭐️ 로컬 개발은 false, 배포(HTTPS) 시 true로 변경!
            .path("/")         // 모든 경로에서 유효
            .maxAge(14 * 24 * 60 * 60) // 14일 (초 단위)
            .sameSite("Strict") // CSRF 방지 (프론트/백 도메인 다르면 "None" 고려)
            .build()
    }

}
