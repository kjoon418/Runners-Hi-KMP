package good.space.runnershi.global.security

import good.space.runnershi.global.security.JwtPlugin
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtPlugin: JwtPlugin
) : OncePerRequestFilter() { // 👈 '한 요청당 한 번만 실행'을 보장하는 필터 상속

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 1. 헤더에서 토큰 꺼내기
        val jwt = getJwtFromRequest(request)

        // 2. 토큰이 있고, 유효하다면?
        if (jwt != null) {
            jwtPlugin.validateToken(jwt)
                .onSuccess { jws ->
                    // 3. 토큰 내용(Claims) 꺼내기
                    val userId = jws.body.subject // id
                    val email = jws.body["email", String::class.java]
                    val role = jws.body["role", String::class.java]

                    // 4. 권한 목록 만들기 (ROLE_ 접두사는 선택사항이지만 Security 표준을 위해 붙이는 게 좋음)
                    // 여기서는 저장된 role 그대로("LOCAL" 등) 사용하거나 필요시 "ROLE_$role"로 변환
                    val authorities = listOf(SimpleGrantedAuthority(role))

                    // 5. 인증 객체(Authentication) 만들기
                    // Principal(신원): 보통 UserDetails 객체를 넣지만, 가볍게 userId나 email을 넣기도 함
                    val authentication = UsernamePasswordAuthenticationToken(
                        userId, // 나중에 Controller에서 @AuthenticationPrincipal로 꺼낼 값
                        null,   // 비밀번호는 이미 인증됐으니 null
                        authorities
                    )

                    // 6. 부가 정보 설정 (IP 주소 등)
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

                    // 7. ⭐️ 가장 중요! SecurityContext에 저장 (이래야 로그인 된 것으로 침)
                    SecurityContextHolder.getContext().authentication = authentication
                }
                .onFailure {
                    // 토큰이 만료되었거나 위조된 경우 에러 로그를 찍거나 무시
                    // 여기서 예외를 던지지 않고 넘어가면, 뒤에 있는 Security 로직이 "너 인증 안 됐네?" 하고 401을 뱉음
                }
        }

        // 8. 다음 필터로 넘기기 (필수!)
        filterChain.doFilter(request, response)
    }

    // 헤더에서 순수 토큰 문자열만 발라내는 함수
    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION)
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7) // "Bearer " 이후의 문자열만 자름
        } else {
            null
        }
    }
}