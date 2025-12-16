package good.space.runnershi.global.security
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig (
    // 👇 생성자로 주입받습니다
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/api/v1/auth/signup",
                    "/api/v1/auth/login",
                    "/api/v1/auth/refresh",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                    .anyRequest().authenticated()
            }
            // ▼▼▼ 여기가 핵심! ▼▼▼
            // "UsernamePasswordAuthenticationFilter(기본 로그인 필터)"보다 "앞(Before)"에 우리 필터를 둡니다.
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    // 비밀번호 암호화 도구 (회원가입/로그인 서비스에서 주입받아 사용)
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}