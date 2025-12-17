package good.space.runnershi.viewmodel

import good.space.runnershi.model.domain.RunResult
import good.space.runnershi.model.dto.running.PersonalBestResponse
import good.space.runnershi.repository.RunRepository
import good.space.runnershi.service.ServiceController
import good.space.runnershi.state.RunningStateManager
import good.space.runnershi.util.format
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RunningViewModel(
    private val serviceController: ServiceController,
    private val runRepository: RunRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    
    // 데이터는 StateManager에서 직접 구독
    val currentLocation: StateFlow<good.space.runnershi.model.domain.LocationModel?> = RunningStateManager.currentLocation
    val totalDistanceMeters: StateFlow<Double> = RunningStateManager.totalDistanceMeters
    val pathSegments: StateFlow<List<List<good.space.runnershi.model.domain.LocationModel>>> = RunningStateManager.pathSegments
    val durationSeconds: StateFlow<Long> = RunningStateManager.durationSeconds
    val isRunning: StateFlow<Boolean> = RunningStateManager.isRunning

    // 결과 화면 표시 여부 및 데이터
    private val _runResult = MutableStateFlow<RunResult?>(null)
    val runResult: StateFlow<RunResult?> = _runResult.asStateFlow()
    
    // 최대 기록 (Personal Best)
    private val _personalBest = MutableStateFlow<PersonalBestResponse?>(null)
    val personalBest: StateFlow<PersonalBestResponse?> = _personalBest.asStateFlow()
    
    init {
        // 앱 시작 시 최대 기록 조회
        fetchPersonalBest()
    }
    
    private fun fetchPersonalBest() {
        scope.launch {
            runRepository.getPersonalBest()
                .onSuccess { record ->
                    _personalBest.value = record
                    println("✅ [RunningViewModel] PB 조회 성공: ${record?.distanceMeters}m")
                }
                .onFailure { e ->
                    // 에러 발생 시 무시 (PB는 선택적 기능)
                    println("⚠️ [RunningViewModel] PB 조회 실패: ${e.message}")
                }
        }
    }

    fun startRun() {
        if (durationSeconds.value > 0) {
            serviceController.resumeService()
        } else {
            serviceController.startService()
        }
    }

    fun pauseRun() {
        serviceController.pauseService()
    }

    fun stopRun() {
        serviceController.stopService()
    }
    
    // 러닝 종료 (통합된 함수)
    fun finishRun() {
        // 1. 현재 상태 캡처
        val result = createRunResultSnapshot()

        // 2. 서비스 종료 및 초기화 (무조건 실행)
        serviceController.stopService()
        RunningStateManager.reset()

        // 3. 결과 화면으로 이동 (무조건 실행)
        // 기록이 짧든 길든 사용자는 "완료 화면"을 보게 됩니다.
        _runResult.value = result

        // 4. 서버 전송 여부 판단 (백그라운드 처리)
        if (shouldUploadToServer(result)) {
            // 조건 충족: 서버에 저장
            uploadRunData(result)
        } else {
            // 조건 미달: 서버 전송 안 함 (로그만 남김)
            // 사용자는 결과 화면을 보고 있지만, 이 데이터는 서버에 남지 않습니다.
            println("⚠️ 기록 미달로 서버 저장 건너뜀 (거리: ${result.totalDistanceMeters}m, 시간: ${result.durationSeconds}초)")
        }
    }
    
    // 서버 전송 조건 검사
    private fun shouldUploadToServer(result: RunResult): Boolean {
        return result.totalDistanceMeters >= 100.0 && result.durationSeconds >= 60
    }

    private fun createRunResultSnapshot(): RunResult {
        val distance = RunningStateManager.totalDistanceMeters.value
        val seconds = RunningStateManager.durationSeconds.value
        
        return RunResult(
            totalDistanceMeters = distance,
            durationSeconds = seconds,
            pathSegments = RunningStateManager.pathSegments.value,
            calories = (distance * 0.06).toInt(), // 단순 예시 계산
            avgPace = calculatePace(distance, seconds)
        )
    }
    
    private fun calculatePace(distanceMeters: Double, seconds: Long): String {
        if (distanceMeters == 0.0) return "00'00''"
        val paceSecondsPerKm = (seconds / (distanceMeters / 1000.0)).toLong()
        val min = paceSecondsPerKm / 60
        val sec = paceSecondsPerKm % 60
        return "%02d'%02d''".format(min, sec)
    }

    private fun uploadRunData(result: RunResult) {
        scope.launch {
            runRepository.saveRun(result)
                .onSuccess { serverId ->
                    println("✅ Upload Success: ID=$serverId")
                    // 필요하다면 여기서 로컬 DB에 '동기화 완료' 마킹
                }
                .onFailure { e ->
                    println("❌ Upload Failed: ${e.message}")
                    // 실패 시 로컬 DB에 저장해두고 나중에 재전송 로직 필요
                }
        }
    }
    
    // 결과 화면 닫기 (메인으로 복귀)
    fun closeResultScreen() {
        _runResult.value = null
    }
}
