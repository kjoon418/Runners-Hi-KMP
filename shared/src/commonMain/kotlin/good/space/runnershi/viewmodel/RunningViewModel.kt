package good.space.runnershi.viewmodel

import good.space.runnershi.model.domain.RunResult
import good.space.runnershi.model.domain.location.LocationModel
import good.space.runnershi.model.dto.running.PersonalBestResponse
import good.space.runnershi.repository.RunRepository
import good.space.runnershi.service.ServiceController
import good.space.runnershi.state.PauseType
import good.space.runnershi.state.RunningStateManager
import good.space.runnershi.model.domain.running.PaceCalculator
import good.space.runnershi.util.format
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class RunningViewModel(
    private val serviceController: ServiceController,
    private val runRepository: RunRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    
    // 러닝 종료 시 RoomDB 초기화를 위한 콜백 (Android에서 주입)
    var onFinishCallback: (suspend () -> Unit)? = null
    
    // 데이터는 StateManager에서 직접 구독
    val currentLocation: StateFlow<LocationModel?> = RunningStateManager.currentLocation
    val totalDistanceMeters: StateFlow<Double> = RunningStateManager.totalDistanceMeters
    val pathSegments: StateFlow<List<List<LocationModel>>> = RunningStateManager.pathSegments
    val durationSeconds: StateFlow<Long> = RunningStateManager.durationSeconds
    val isRunning: StateFlow<Boolean> = RunningStateManager.isRunning
    val pauseType: StateFlow<PauseType> = RunningStateManager.pauseType
    val vehicleWarningCount: StateFlow<Int> = RunningStateManager.vehicleWarningCount

    // [New] 실시간 페이스 계산 (StateFlow)
    // 오직 '거리'가 변경될 때만 페이스를 재계산합니다.
    // 시간은 흐르고 있지만, 계산 시점의 값만 "조회(Snapshot)"해서 사용합니다.
    // 이렇게 하면 GPS 위치가 잡힐 때만 페이스가 갱신되어, 신호 대기 중이거나 멈춰 있을 때 페이스가 계속 느려지는 현상을 방지합니다.
    val currentPace: StateFlow<String> = totalDistanceMeters
        .map { distance ->
            // 시간은 흐르고 있지만, 계산 시점의 값만 "조회(Snapshot)"해서 씁니다.
            val currentDuration = durationSeconds.value
            PaceCalculator.calculatePace(distance, currentDuration)
        }
        .distinctUntilChanged() // 동일한 페이스 값이면 재방출 방지 (성능 최적화 및 무한 재구성 방지)
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "-'--''"
        )

    // 결과 화면 표시 여부 및 데이터
    private val _runResult = MutableStateFlow<RunResult?>(null)
    val runResult: StateFlow<RunResult?> = _runResult.asStateFlow()
    
    // 최대 기록 (Personal Best)
    private val _personalBest = MutableStateFlow<PersonalBestResponse?>(null)
    val personalBest: StateFlow<PersonalBestResponse?> = _personalBest.asStateFlow()
    
    // 업로드 상태 관리
    private val _uploadState = MutableStateFlow(UploadState.IDLE)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()
    
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

        // 4. 업로드 상태 초기화 (재진입 고려)
        _uploadState.value = UploadState.IDLE

        // 5. 서버 전송 여부 판단 (백그라운드 처리)
        if (shouldUploadToServer(result)) {
            // 조건 충족: 서버에 저장
            uploadRunData(result)
        } else {
            // 조건 미달: 서버 전송 안 함 (로그만 남김)
            // 사용자는 결과 화면을 보고 있지만, 이 데이터는 서버에 남지 않습니다.
            println("⚠️ 기록 미달로 서버 저장 건너뜀 (거리: ${result.totalDistanceMeters}m, 시간: ${result.duration.inWholeSeconds}초)")
            // RoomDB 데이터 삭제 (쓰레기 데이터 방지)
            scope.launch {
                onFinishCallback?.invoke()
            }
        }
    }
    
    // 서버 전송 조건 검사
    private fun shouldUploadToServer(result: RunResult): Boolean {
        return result.totalDistanceMeters >= 300.0 && result.duration.inWholeSeconds >= 180
    }

    private fun createRunResultSnapshot(): RunResult {
        val distance = RunningStateManager.totalDistanceMeters.value
        val durationSeconds = RunningStateManager.durationSeconds.value
        val startTime = RunningStateManager.startTime.value
        val finishedAt = Clock.System.now()
        
        // duration: 실제 러닝 시간 (PAUSE 시간 제외)
        val duration = durationSeconds.toDuration(DurationUnit.SECONDS)
        
        // totalTime: 휴식시간을 포함한 총 시간 (시작부터 종료까지)
        val totalTime = if (startTime != null) {
            val calculated = finishedAt - startTime
            // 방어 로직: 음수나 0이면 duration을 사용 (최소한의 값 보장)
            if (calculated.isPositive()) calculated else duration
        } else {
            // startTime이 null인 경우 (비정상 상황, 방어 코드)
            println("⚠️ [RunningViewModel] startTime이 null입니다. duration을 사용합니다.")
            duration
        }
        
        // 추가 검증: totalTime이 duration보다 작으면 duration 사용
        val finalTotalTime = if (totalTime >= duration) {
            totalTime
        } else {
            println("⚠️ [RunningViewModel] totalTime($totalTime) < duration($duration). duration을 사용합니다.")
            duration
        }
        
        return RunResult(
            totalDistanceMeters = distance,
            duration = duration,
            totalTime = finalTotalTime,
            pathSegments = RunningStateManager.pathSegments.value,
            calories = (distance * 0.06).toInt(), // 단순 예시 계산
            startedAt = startTime ?: finishedAt // null이면 현재 시간 사용
            // movingPace와 elapsedPace는 RunResult 모델에서 자동 계산됨
        )
    }

    private fun uploadRunData(result: RunResult) {
        scope.launch {
            _uploadState.value = UploadState.UPLOADING // 로딩 시작
            
            runRepository.saveRun(result)
                .onSuccess { serverId ->
                    _uploadState.value = UploadState.SUCCESS // 성공!
                    println("✅ Upload Success: ID=$serverId")
                    // 서버 저장 성공 시 RoomDB 데이터 삭제 (이미 서버에 저장됨)
                    onFinishCallback?.invoke()
                }
                .onFailure { e ->
                    _uploadState.value = UploadState.FAILURE // 실패...
                    println("❌ Upload Failed: ${e.message}")
                    // 실패 시 로컬 DB에 저장해두고 나중에 재전송 로직 필요
                    // TODO: 실패 시 RoomDB 데이터는 유지 (재전송을 위해)
                }
        }
    }
    
    // 결과 화면 닫기 (메인으로 복귀)
    fun closeResultScreen() {
        _runResult.value = null
    }
}
