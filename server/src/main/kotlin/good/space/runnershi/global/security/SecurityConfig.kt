package good.space.runnershi.global.security
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig (
    // 👇 생성자로 주입받습니다
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    companion object {
        private val ALLOWED_ORIGINS = listOf(
            "http://localhost:3000",
            "http://localhost:5173",
            "https://runners-hi.site",
            "https://api.runners-hi.site",
            "https://runners-hi-front-end.vercel.app"
        )

        private val ALLOWED_METHODS = listOf(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        )

        private val ALLOWED_HEADERS = listOf(
            "*"
        )

        private val EXPOSED_HEADERS = listOf(
            "Authorization"
        )

        private const val ALLOW_CREDENTIALS = true

        // Security permitAll paths
        private val PERMIT_ALL_PATHS = arrayOf(
            "/api/v1/auth/**",
            "/api/v1/running/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
        )
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource())}
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                it.requestMatchers(*PERMIT_ALL_PATHS).permitAll()
                it.anyRequest().authenticated()
            }
            // ▼▼▼ 여기가 핵심! ▼▼▼
            // "UsernamePasswordAuthenticationFilter(기본 로그인 필터)"보다 "앞(Before)"에 우리 필터를 둡니다.
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = ALLOWED_ORIGINS
            allowedMethods = ALLOWED_METHODS
            allowedHeaders = ALLOWED_HEADERS
            exposedHeaders = EXPOSED_HEADERS
            allowCredentials = ALLOW_CREDENTIALS
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }

    // 비밀번호 암호화 도구 (회원가입/로그인 서비스에서 주입받아 사용)
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
