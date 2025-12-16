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
    
    // 러닝 종료 버튼을 눌렀을 때 호출
    fun finishRun() {
        // 1. 현재 상태를 스냅샷으로 저장 (데이터 캡처)
        val result = createRunResultSnapshot()
        
        // 2. 서비스 및 위치 추적 완전 종료
        serviceController.stopService()
        
        // 3. 상태 매니저 초기화 (다음 러닝을 위해)
        RunningStateManager.reset()

        // 4. 결과 화면에 데이터 전달 (UI 전환 트리거)
        _runResult.value = result

        // 5. (비동기) 서버로 데이터 전송
        uploadRunData(result)
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
