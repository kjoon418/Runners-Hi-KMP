package good.space.runnershi.auth

import good.space.runnershi.global.security.JwtPlugin
import good.space.runnershi.model.dto.auth.LoginRequest
import good.space.runnershi.model.dto.auth.SignUpRequest
import good.space.runnershi.model.dto.auth.TokenRefreshResponse
import good.space.runnershi.model.dto.auth.TokenResponse
import good.space.runnershi.user.domain.LocalUser
import good.space.runnershi.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder, // SecurityConfig에서 등록한 그 친구
    private val jwtPlugin: JwtPlugin,             // 방금 만든 토큰 공장
    private val refreshTokenRepository: RefreshTokenRepository
) {

    @Transactional
    fun signUp(request: SignUpRequest) {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("이미 사용 중인 이메일입니다.")
        }

        val encodedPassword = passwordEncoder.encode(request.password)
        val newUser = LocalUser(
            email = request.email,
            name = request.name,
            password = encodedPassword,
            sex = request.sex
        )

        // 4. DB 저장
        userRepository.save(newUser)
    }

    @Transactional
    fun login(request: LoginRequest): TokenResponse {
        // 1. 이메일로 유저 찾기
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.")

        // 2. 유저 타입 확인 (LocalUser인지)
        // 소셜 로그인 유저는 비밀번호가 없으므로 여기서 걸러낼 수도 있음
        if (user !is LocalUser) {
            throw IllegalArgumentException("소셜 로그인으로 가입된 계정입니다.")
        }

        // 3. 비밀번호 검증 (입력받은 비번 vs DB에 있는 암호화된 비번 비교)
        // matches(평문, 암호문) 순서 중요!
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.")
        }

        // 4. 토큰 발급
        val accessToken = jwtPlugin.generateAccessToken(
            subject = user.id.toString(), // 토큰 주인(ID)
            email = user.email,
            role = user.userType.name     // "LOCAL"
        )

        val refreshToken = jwtPlugin.generateRefreshToken(
            subject = user.id.toString(),
            email = user.email,
            role = user.userType.name
        )

        // 3. Refresh Token 저장 (이미 있으면 업데이트, 없으면 생성)
        val existingToken = refreshTokenRepository.findByUserId(user.id!!)
        if (existingToken != null) {
            existingToken.updateToken(refreshToken)
        } else {
            refreshTokenRepository.save(RefreshToken(user = user, token = refreshToken))
        }

        return TokenResponse(accessToken = accessToken, refreshToken = refreshToken)
    }

    @Transactional
    fun refreshAccessToken(refreshToken: String): TokenRefreshResponse {
        // 1. Refresh Token 검증
        val verifiedToken = jwtPlugin.validateToken(refreshToken)
            .getOrElse { throw IllegalArgumentException("유효하지 않은 Refresh Token입니다.") }

        // 2. DB에 저장된 토큰과 일치하는지 확인 (탈취 방지)
        val userId = verifiedToken.body.subject.toLong()
        val savedToken = refreshTokenRepository.findByUserId(userId)
            ?: throw IllegalArgumentException("로그아웃된 사용자입니다.")

        if (savedToken.token != refreshToken) {
            throw IllegalArgumentException("토큰이 일치하지 않습니다.")
        }

        // 3. 유저 정보 가져오기
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        // 4. 새로운 Access Token 발급
        val newAccessToken = jwtPlugin.generateAccessToken(
            subject = user.id.toString(),
            email = user.email,
            role = user.userType.name
        )

        return TokenRefreshResponse(newAccessToken, refreshToken)
    }

    @Transactional
    fun logout(userId: Long) {
        refreshTokenRepository.deleteByUserId(userId)
    }
}
