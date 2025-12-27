package good.space.runnershi.auth
import good.space.runnershi.model.dto.auth.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Auth", description = "인증/인가 (회원가입, 로그인, 로그아웃) API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 이름 등을 입력받아 새로운 회원을 등록합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "회원가입 성공"),
        ApiResponse(responseCode = "400", description = "입력값 오류 또는 이미 존재하는 이메일")
    ])
    @PostMapping("/signup")
    fun signUp(@RequestBody request: SignUpRequest): ResponseEntity<String> {
        authService.signUp(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body("회원가입 성공!")
    }

    @Operation(summary = "로그인 (모바일/일반)", description = "이메일과 비밀번호로 로그인합니다. Access Token과 Refresh Token을 모두 Response Body로 반환합니다.")
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        val token = authService.login(request)
        return ResponseEntity.ok(token)
    }

    @Operation(summary = "토큰 갱신 (모바일/일반)", description = "만료된 Access Token을 대신해, **Body**로 받은 Refresh Token을 사용하여 새로운 토큰을 발급받습니다.")
    @PostMapping("/refresh")
    fun refresh(@RequestBody request: TokenRefreshRequest): ResponseEntity<TokenRefreshResponse> {
        val token = authService.refreshAccessToken(request.refreshToken)
        return ResponseEntity.ok(token)
    }

    @Operation(summary = "로그아웃 (모바일/일반)", description = "서버 DB에서 Refresh Token을 삭제하여 더 이상 토큰 갱신이 불가능하게 만듭니다. (클라이언트 측 Access Token 삭제 필요)")
    @PostMapping("/logout")
    fun logout(
        @Parameter(hidden = true) // 👈 Swagger UI에 입력창 안 뜨게 숨김
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<String> {
        authService.logout(userId)
        return ResponseEntity.ok("로그아웃 되었습니다.")
    }

    @Operation(summary = "로그인 (웹 전용)", description = "웹 환경 로그인입니다. Refresh Token은 HttpOnly Cookie에 설정되고, Body에는 Access Token만 반환됩니다 (보안 강화).")
    @PostMapping("/login/web")
    fun loginWeb(@RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        val originalToken = authService.login(request)

        val cookie = createRefreshTokenCookie(originalToken.refreshToken ?: "")

        // 웹 보안: Body에 나갈 Refresh Token 제거
        val secureTokenResponse = originalToken.copy(refreshToken = null)

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(secureTokenResponse)
    }

    @Operation(summary = "토큰 갱신 (웹 전용)", description = "**Cookie**에 저장된 Refresh Token을 감지하여 새로운 토큰을 발급합니다. 갱신된 Refresh Token은 다시 쿠키에 저장됩니다.")
    @PostMapping("/refresh/web")
    fun refreshWeb(
        @Parameter(hidden = true) // 👈 쿠키는 브라우저가 알아서 보내므로 숨김 처리 (선택사항)
        @CookieValue("refreshToken") refreshToken: String
    ): ResponseEntity<TokenRefreshResponse> {
        val newTokenResponse = authService.refreshAccessToken(refreshToken)

        val cookie = createRefreshTokenCookie(newTokenResponse.refreshToken)

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            // 주의: DTO에 refreshToken 필드가 있다면 여기서도 copy(refreshToken = null) 해주는 게 더 완벽함
            .body(newTokenResponse)
    }

    @Operation(summary = "로그아웃 (웹 전용)", description = "서버 DB에서 토큰을 삭제하고, 브라우저의 **Refresh Token 쿠키를 만료(삭제)**시킵니다.")
    @PostMapping("/logout/web")
    fun logoutWeb(
        @Parameter(hidden = true)
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<String> {

        authService.logout(userId)
        val cookie = createRefreshTokenCookie(null) // 쿠키 삭제용 (maxAge = 0)

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body("로그아웃 되었습니다.")
    }

    private fun createRefreshTokenCookie(refreshToken: String?): ResponseCookie {
        if (refreshToken.isNullOrBlank()) {
            return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build()
        }

        return ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(14 * 24 * 60 * 60)
            .sameSite("Strict")
            .build()
    }
}
