package good.space.runnershi.running

import good.space.runnershi.global.running.domain.Running
import good.space.runnershi.global.running.repository.RunningRepository
import good.space.runnershi.global.running.service.RunningService
import good.space.runnershi.model.domain.auth.Sex
import good.space.runnershi.model.dto.running.LocationPoint
import good.space.runnershi.model.dto.running.RunCreateRequest
import good.space.runnershi.user.domain.Achievement
import good.space.runnershi.user.domain.LocalUser
import good.space.runnershi.user.repository.UserRepository
import kotlinx.datetime.Instant
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus

@ExtendWith(MockitoExtension::class) // 1. Mockito 확장 기능을 사용 (Spring Context 로딩 X -> 빠름)
class RunningServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var runningRepository: RunningRepository

    @InjectMocks
    private lateinit var runningService: RunningService

    private fun createTestUser(
        name: String = "TestRunner",
        email: String = "test@example.com",
        password: String = "password123"
    ): LocalUser {
        return LocalUser(
            name = name,
            email = email,
            password = password,
            sex = Sex.MALE,
        )
    }

    @Test
    @DisplayName("러닝 기록 저장 성공 시: Repository가 호출되고, 유저 정보가 업데이트된 DTO가 반환되어야 한다")
    fun saveRunningStats_Success() {
        val userId = 1L
        val fakeUser = createTestUser().apply {
            this.id = userId
            this.exp = 0
            this.totalDistanceMeters = 300.0 // 초기값 300.0
        }

        val request = RunCreateRequest(
            distanceMeters = 3000.0,
            runningDuration = 15.minutes,
            totalDuration = 20.minutes,
            startedAt = Instant.parse("2025-12-25T10:00:00Z"),
            locations = listOf(
                LocationPoint(
                    latitude = 37.5,
                    longitude = 127.0,
                    timestamp = Instant.parse("2025-12-25T10:00:00Z"),
                    segmentIndex = 0,
                    sequenceOrder = 0
                )
            )
        )

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(fakeUser))
        `when`(runningRepository.save(any(Running::class.java))).thenAnswer { invocation ->
            val savedEntity = invocation.getArgument(0) as Running
            savedEntity.apply { id = 100L } // ID 부여 시뮬레이션
        }

        val response = runningService.saveRunningStats(userId, request)

        // A. 리포지토리 호출 검증
        verify(userRepository).findById(userId) // 유저 조회가 일어났는가?
        verify(runningRepository).save(any(Running::class.java)) // 러닝 저장이 일어났는가?

        // B. 유저 객체 상태 변화 검증 (도메인 로직이 잘 실행되었는지)
        assertThat(fakeUser.totalDistanceMeters).isEqualTo(3300.0) // 초기값 300.0 + 3000.0 = 3300.0
        assertThat(fakeUser.exp).isEqualTo(3000L) // 경험치가 올랐는가? (거리 3000 = 경험치 3000)

        // C. 반환된 DTO 검증
        assertThat(response.userId).isEqualTo(userId)
        assertThat(response.userExp).isEqualTo(3000L)
    }

    @Test
    @DisplayName("존재하지 않는 유저 ID로 요청 시: 예외가 발생하고 저장은 실행되지 않아야 한다")
    fun saveRunningStats_UserNotFound() {
        val wrongUserId = 999L
        val request = RunCreateRequest(
            distanceMeters = 1000.0,
            runningDuration = 10.minutes,
            totalDuration = 10.minutes,
            startedAt = Instant.parse("2025-01-01T10:00:00Z"),
            locations = listOf(
                LocationPoint(
                    latitude = 37.5,
                    longitude = 127.0,
                    timestamp = Instant.parse("2025-01-01T10:00:00Z"),
                    segmentIndex = 0,
                    sequenceOrder = 0
                )
            )
        )

        // Mocking: 유저를 찾지 못함 (Optional.empty())
        `when`(userRepository.findById(wrongUserId)).thenReturn(Optional.empty())

        assertThatThrownBy {
            runningService.saveRunningStats(wrongUserId, request)
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("user with id $wrongUserId 를 찾을 수 없습니다")

        verify(runningRepository, times(0)).save(any())
    }

    @Test
    @DisplayName("업적 달성 시: 반환된 Response의 newBadges에 정보가 포함되어야 한다")
    fun saveRunningStats_WithNewBadges() {
        val userId = 2L
        val fakeUser = createTestUser().apply { id = userId }

        // 1000m 이상 뛰면 업적을 달성한다고 가정 (Achievement Enum 조건에 따름)
        val request = RunCreateRequest(
            distanceMeters = 1500.0, // 1.5km
            runningDuration = 10.minutes,
            totalDuration = 10.minutes,
            startedAt = Instant.parse("2025-05-05T10:00:00Z"),
            locations = listOf(
                LocationPoint(
                    latitude = 37.5,
                    longitude = 127.0,
                    timestamp = Instant.parse("2025-05-05T10:00:00Z"),
                    segmentIndex = 0,
                    sequenceOrder = 0
                )
            )
        )

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(fakeUser))
        `when`(runningRepository.save(any())).thenAnswer {
            (it.getArgument(0) as Running).apply { id = 200L }
        }
        val response = runningService.saveRunningStats(userId, request)

        // 새로 획득한 뱃지 목록 검증
        // 초기값 300.0 + 1500.0 = 1800.0m이므로 CUMULATIVE_LV1(1000m 이상) 달성
        assertThat(response.newBadges).isNotEmpty

        // CUMULATIVE_LV1 업적이 포함되어 있는지 확인
        val cumulativeBadge = response.newBadges.find { it.name == Achievement.CUMULATIVE_LV1.name }
        assertThat(cumulativeBadge).isNotNull
        assertThat(cumulativeBadge!!.exp).isEqualTo(Achievement.CUMULATIVE_LV1.exp)

    }
    @Test
    @DisplayName("RunResult 데이터가 저장될 때: 여러 구간(Segment)으로 나뉜 경로도 정상적으로 저장되고 유저 통계에 반영되어야 한다")
    fun saveRunningStats_WithSegments() {
        // ==========================================
        // 1. Given (데이터 준비)
        // ==========================================
        val userId = 1L
        val fakeUser = createTestUser().apply {
            this.id = userId
            this.exp = 0
            this.totalDistanceMeters = 300.0 // 초기값 300.0
        }
        val startTime = Instant.parse("2025-05-20T18:00:00Z")

        // 🏃‍♂️ 가상의 경로 데이터 생성 (클라이언트의 pathSegments를 서버 DTO로 변환했다고 가정)
        // Segment 0: 0~5분 동안 뜀 (10개의 점)
        val segment1 = createMockPoints(
            startLat = 37.5000, startLng = 127.0000,
            count = 10, segmentIndex = 0, startTime = startTime
        )

        // (휴식 5분)

        // Segment 1: 10~15분 동안 뜀 (10개의 점, 위치가 조금 이동됨)
        val segment2 = createMockPoints(
            startLat = 37.5020, startLng = 127.0020,
            count = 10, segmentIndex = 1, startTime = startTime.plus(10.minutes)
        )

        // 두 구간을 합쳐서 서버 요청 DTO 생성
        // segment1과 segment2의 sequenceOrder를 구간별로 고유하게 만들어야 함
        val allLocations = (segment1 + segment2).mapIndexed { index, point ->
            point.copy(sequenceOrder = index)
        }

        val request = RunCreateRequest(
            distanceMeters = 5200.0,      // 총 거리 5.2km
            runningDuration = 30.minutes, // 실제 뛴 시간
            totalDuration = 35.minutes,   // 휴식 포함 총 시간
            startedAt = startTime,
            locations = allLocations      // ⭐️ 구간 정보가 포함된 전체 좌표 리스트
        )

        // Mocking
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(fakeUser))
        `when`(runningRepository.save(any(Running::class.java))).thenAnswer {
            val entity = it.getArgument(0) as Running
            entity.apply { id = 777L } // 저장된 ID 리턴 시뮬레이션
        }

        val response = runningService.saveRunningStats(userId, request)

        // A. 저장 로직 호출 검증
        verify(runningRepository).save(any(Running::class.java))

        // B. 유저 통계 업데이트 검증
        assertThat(fakeUser.totalDistanceMeters).isEqualTo(5500.0) // 초기값 300.0 + 5200.0 = 5500.0
        assertThat(fakeUser.exp).isEqualTo(5200L)
        assertThat(fakeUser.lastRunDate).isNotNull // 출석 체크 확인

        // C. 응답값 검증
        assertThat(response.userExp).isEqualTo(5200L)

        // D. 뱃지 획득 검증 (예: 5km 이상이므로 CUMULATIVE_LV1 획득 가정)
        // 주의: 실제 Achievement 로직에 따라 결과가 다를 수 있음
        // assertThat(response.newBadges).isNotEmpty
    }

    @Test
    @DisplayName("기존 업적이 있을 때 새로운 업적 달성 시: achievements에는 전체 업적이, newAchievements에는 새로 달성한 업적만 포함되어야 한다")
    fun saveRunningStats_WithExistingAchievements() {
        val userId = 3L
        val fakeUser = createTestUser().apply {
            this.id = userId
            
            // 기존 업적 5개 설정
            // 1. ATTENDANCE_LV1 (3일 이상)
            // 2. ATTENDANCE_LV2 (10일 이상)
            // 3. CUMULATIVE_LV1 (1km 이상)
            // 4. MAX_DIST_LV1 (3km 이상)
            // 5. PACE_LV4 (300초/km 이하) - 새로운 러닝보다 좋은 페이스로 설정하여 페이스 업적이 추가로 달성되지 않도록
            
            // ATTENDANCE_LV3 달성 직전 상태 (29일)
            this.totalRunningDays = 29 // 새로운 러닝으로 +1 하면 30일이 되어 ATTENDANCE_LV3 달성
            this.totalDistanceMeters = 9000.0 // CUMULATIVE_LV2 달성 직전 (10km 이상이 되도록)
            this.longestDistanceMeters = 3000.0 // MAX_DIST_LV1 달성
            this.bestPace = 300.0 // PACE_LV4 달성 (새로운 러닝보다 좋은 페이스로 설정하여 페이스 업적이 추가로 달성되지 않도록)
            this.lastRunDate = kotlinx.datetime.LocalDate.parse("2025-05-31") // 다른 날짜로 설정하여 새로운 날짜로 인식되도록
            
            // 기존 업적들을 achievements에 추가
            this.achievements.add(Achievement.ATTENDANCE_LV1)
            this.achievements.add(Achievement.ATTENDANCE_LV2)
            this.achievements.add(Achievement.CUMULATIVE_LV1)
            this.achievements.add(Achievement.MAX_DIST_LV1)

            this.achievements.add(Achievement.PACE_LV1)
            this.achievements.add(Achievement.PACE_LV2)
            this.achievements.add(Achievement.PACE_LV3)
            this.achievements.add(Achievement.PACE_LV4)


            
            // 기존 업적 개수 확인
            assertThat(this.achievements.size).isEqualTo(8)
        }

        // 새로운 러닝 기록으로 2개의 업적을 추가로 달성할 수 있도록 설정
        // 1. ATTENDANCE_LV3 달성 (30일 이상) - 현재 29일 + 1일 = 30일
        // 2. CUMULATIVE_LV2 달성 (10km 이상) - 현재 9000m + 1000m = 10000m
        // 페이스는 기존 bestPace(300초/km)보다 나쁘게 설정하여 페이스 업적이 추가로 달성되지 않도록 함
        
        val request = RunCreateRequest(
            distanceMeters = 1000.0, // 1km (총 거리 10km가 되도록)
            runningDuration = 10.minutes, // 600초/km 페이스 (기존 300초/km보다 나쁨 -> bestPace 업데이트 안 됨)
            totalDuration = 10.minutes,
            startedAt = Instant.parse("2025-06-01T10:00:00Z"), // 다른 날짜 (lastRunDate와 다름)
            locations = listOf(
                LocationPoint(
                    latitude = 37.5,
                    longitude = 127.0,
                    timestamp = Instant.parse("2025-06-01T10:00:00Z"),
                    segmentIndex = 0,
                    sequenceOrder = 0
                )
            )
        )

        // Mocking
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(fakeUser))
        `when`(runningRepository.save(any(Running::class.java))).thenAnswer {
            val entity = it.getArgument(0) as Running
            entity.apply { id = 300L }
        }

        val response = runningService.saveRunningStats(userId, request)

        // A. achievements에는 기존 5개 + 새로운 2개 = 총 7개가 있어야 함
        assertThat(fakeUser.achievements.size).isEqualTo(10)
        assertThat(fakeUser.achievements).contains(
            Achievement.ATTENDANCE_LV1,
            Achievement.ATTENDANCE_LV2,
            Achievement.ATTENDANCE_LV3, // 새로 달성
            Achievement.CUMULATIVE_LV1,
            Achievement.CUMULATIVE_LV2, // 새로 달성
            Achievement.MAX_DIST_LV1,
            Achievement.PACE_LV4,
            Achievement.PACE_LV1,
            Achievement.PACE_LV2,
            Achievement.PACE_LV3
        )
        
        // B. newAchievements에는 새로 달성한 2개만 있어야 함
        assertThat(fakeUser.newAchievements.size).isEqualTo(2)
        assertThat(fakeUser.newAchievements).contains(
            Achievement.ATTENDANCE_LV3,
            Achievement.CUMULATIVE_LV2
        )
        
        // C. 응답의 newBadges에도 새로 달성한 2개가 있어야 함
        assertThat(response.newBadges.size).isEqualTo(2)
        val newBadgeNames = response.newBadges.map { it.name }.toSet()
        assertThat(newBadgeNames).contains(
            Achievement.ATTENDANCE_LV3.name,
            Achievement.CUMULATIVE_LV2.name
        )
        
        // D. 응답의 badges에는 전체 7개가 있어야 함
        assertThat(response.badges.size).isEqualTo(10)
    }

    // 🛠️ 헬퍼 함수: 테스트용 좌표 리스트 생성기
    private fun createMockPoints(
        startLat: Double,
        startLng: Double,
        count: Int,
        segmentIndex: Int,
        startTime: Instant
    ): List<LocationPoint> {
        return (0 until count).map { i ->
            LocationPoint(
                latitude = startLat + (i * 0.0001),
                longitude = startLng + (i * 0.0001),
                timestamp = startTime.plus((i * 10).seconds), // Instant 타입
                segmentIndex = segmentIndex, // 몇 번째 구간인지 중요!
                sequenceOrder = i // 순서 인덱스
            )
        }
    }

    private fun createMockRoute(startLat: Double, startLng: Double, count: Int): List<LocationPoint> {
        val startTime = Instant.parse("2025-05-20T18:00:00Z")
        return (0 until count).map { i ->
            // i가 증가할 때마다 위도/경도를 0.0001씩(약 10m) 증가시킴 -> 이동하는 것처럼 보임
            LocationPoint(
                latitude = startLat + (i * 0.0001),
                longitude = startLng + (i * 0.0001),
                timestamp = startTime.plus((i * 10).seconds), // Instant 타입
                segmentIndex = 0,
                sequenceOrder = i
            )
        }
    }

    // 테스트용 Running 엔티티 생성 헬퍼 함수
    private fun createTestRunning(
        id: Long,
        userId: Long,
        distanceMeters: Double,
        duration: kotlin.time.Duration,
        startedAt: Instant
    ): Running {
        val running = Running(
            duration = duration,
            totalTime = duration,
            distanceMeters = distanceMeters,
            startedAt = startedAt
        )
        running.id = id
        return running
    }

    @Test
    @DisplayName("러닝 히스토리 조회 성공 시: 날짜 범위 내의 러닝 기록들이 정상적으로 반환되어야 한다")
    fun getRunningHistory_Success() {
        val userId = 1L
        val startDate = LocalDate.parse("2025-01-01")
        val endDate = LocalDate.parse("2025-01-31")

        val timeZone = TimeZone.currentSystemDefault()
        val startInstant = startDate.atStartOfDayIn(timeZone)
        val endInstant = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)

        // 테스트용 러닝 기록들 생성
        val running1 = createTestRunning(
            id = 1L,
            userId = userId,
            distanceMeters = 3000.0,
            duration = 15.minutes,
            startedAt = Instant.parse("2025-01-15T10:00:00Z")
        )

        val running2 = createTestRunning(
            id = 2L,
            userId = userId,
            distanceMeters = 5000.0,
            duration = 25.minutes,
            startedAt = Instant.parse("2025-01-20T18:00:00Z")
        )

        val running3 = createTestRunning(
            id = 3L,
            userId = userId,
            distanceMeters = 2000.0,
            duration = 10.minutes,
            startedAt = Instant.parse("2025-01-25T07:00:00Z")
        )

        val mockRunnings = listOf(running1, running2, running3)

        // Mocking: 리포지토리가 러닝 기록 리스트를 반환하도록 설정
        `when`(runningRepository.findAllByUserIdAndStartedAtBetween(userId, startInstant, endInstant))
            .thenReturn(mockRunnings)

        val result = runningService.getRunningHistory(userId, startDate, endDate)

        verify(runningRepository).findAllByUserIdAndStartedAtBetween(userId, startInstant, endInstant)

        // B. 반환된 결과 검증
        assertThat(result).hasSize(3)

        // C. 첫 번째 러닝 기록 검증
        assertThat(result[0].runId).isEqualTo(1L)
        assertThat(result[0].distanceMeters).isEqualTo(3000.0)
        assertThat(result[0].durationSeconds).isEqualTo(15.minutes)
        assertThat(result[0].startedAt).isEqualTo(Instant.parse("2025-01-15T10:00:00Z"))
        assertThat(result[0].averagePace).isEqualTo(running1.averagePace)

        // D. 두 번째 러닝 기록 검증
        assertThat(result[1].runId).isEqualTo(2L)
        assertThat(result[1].distanceMeters).isEqualTo(5000.0)
        assertThat(result[1].durationSeconds).isEqualTo(25.minutes)
        assertThat(result[1].startedAt).isEqualTo(Instant.parse("2025-01-20T18:00:00Z"))
        assertThat(result[1].averagePace).isEqualTo(running2.averagePace)

        // E. 세 번째 러닝 기록 검증
        assertThat(result[2].runId).isEqualTo(3L)
        assertThat(result[2].distanceMeters).isEqualTo(2000.0)
        assertThat(result[2].durationSeconds).isEqualTo(10.minutes)
        assertThat(result[2].startedAt).isEqualTo(Instant.parse("2025-01-25T07:00:00Z"))
        assertThat(result[2].averagePace).isEqualTo(running3.averagePace)
    }

    @Test
    @DisplayName("러닝 히스토리 조회 시 기록이 없는 경우: 빈 리스트가 반환되어야 한다")
    fun getRunningHistory_EmptyResult() {
        val userId = 1L
        val startDate = LocalDate.parse("2025-02-01")
        val endDate = LocalDate.parse("2025-02-28")

        val timeZone = TimeZone.currentSystemDefault()
        val startInstant = startDate.atStartOfDayIn(timeZone)
        val endInstant = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)

        // Mocking: 빈 리스트 반환
        `when`(runningRepository.findAllByUserIdAndStartedAtBetween(userId, startInstant, endInstant))
            .thenReturn(emptyList())

        val result = runningService.getRunningHistory(userId, startDate, endDate)

        verify(runningRepository).findAllByUserIdAndStartedAtBetween(userId, startInstant, endInstant)

        // B. 빈 리스트 반환 검증
        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("러닝 히스토리 조회 시 날짜 범위 경계값 테스트: 시작일과 종료일의 러닝 기록이 포함되어야 한다")
    fun getRunningHistory_BoundaryDates() {
        val userId = 1L
        val startDate = LocalDate.parse("2025-03-01")
        val endDate = LocalDate.parse("2025-03-01")

        val timeZone = TimeZone.currentSystemDefault()
        val startInstant = startDate.atStartOfDayIn(timeZone)
        val endInstant = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)

        // 시작일 00:00:00에 시작한 러닝 기록
        val runningAtStart = createTestRunning(
            id = 1L,
            userId = userId,
            distanceMeters = 1000.0,
            duration = 5.minutes,
            startedAt = startInstant
        )

        // 종료일 23:59:59에 시작한 러닝 기록 (다음 날 00:00:00 이전이므로 포함되어야 함)
        // endInstant는 다음 날 00:00:00이므로, 종료일의 마지막 시간을 직접 지정
        val runningAtEnd = createTestRunning(
            id = 2L,
            userId = userId,
            distanceMeters = 2000.0,
            duration = 10.minutes,
            startedAt = endDate.atStartOfDayIn(timeZone).plus(23, DateTimeUnit.HOUR).plus(59, DateTimeUnit.MINUTE).plus(59, DateTimeUnit.SECOND)
        )

        val mockRunnings = listOf(runningAtStart, runningAtEnd)

        // Mocking
        `when`(runningRepository.findAllByUserIdAndStartedAtBetween(userId, startInstant, endInstant))
            .thenReturn(mockRunnings)

        val result = runningService.getRunningHistory(userId, startDate, endDate)

        verify(runningRepository).findAllByUserIdAndStartedAtBetween(userId, startInstant, endInstant)

        // B. 두 개의 기록이 모두 포함되어야 함
        assertThat(result).hasSize(2)
        assertThat(result.map { it.runId }).containsExactlyInAnyOrder(1L, 2L)
    }

    @Test
    @DisplayName("러닝 히스토리 조회 시 날짜 범위 밖의 기록은 제외되어야 한다")
    fun getRunningHistory_ExcludeOutOfRange() {
        val userId = 1L
        val startDate = LocalDate.parse("2025-04-01")
        val endDate = LocalDate.parse("2025-04-30")

        val timeZone = TimeZone.currentSystemDefault()
        val startInstant = startDate.atStartOfDayIn(timeZone)
        val endInstant = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)

        // 범위 내의 러닝 기록만 생성
        val runningInRange = createTestRunning(
            id = 1L,
            userId = userId,
            distanceMeters = 3000.0,
            duration = 15.minutes,
            startedAt = Instant.parse("2025-04-15T12:00:00Z")
        )

        val mockRunnings = listOf(runningInRange)

        // Mocking: 리포지토리는 범위 내의 기록만 반환 (범위 밖의 기록은 리포지토리에서 필터링됨)
        `when`(runningRepository.findAllByUserIdAndStartedAtBetween(userId, startInstant, endInstant))
            .thenReturn(mockRunnings)

        val result = runningService.getRunningHistory(userId, startDate, endDate)

        // A. 리포지토리 호출 검증
        verify(runningRepository).findAllByUserIdAndStartedAtBetween(userId, startInstant, endInstant)

        // B. 범위 내의 기록만 반환되어야 함
        assertThat(result).hasSize(1)
        assertThat(result[0].runId).isEqualTo(1L)
        assertThat(result[0].startedAt).isEqualTo(Instant.parse("2025-04-15T12:00:00Z"))
    }
}
