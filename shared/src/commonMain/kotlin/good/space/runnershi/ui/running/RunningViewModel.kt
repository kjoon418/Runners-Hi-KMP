package good.space.runnershi.ui.running

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import good.space.runnershi.model.domain.RunResult
import good.space.runnershi.model.domain.location.LocationModel
import good.space.runnershi.model.domain.running.PaceCalculator
import good.space.runnershi.model.dto.running.LongestDistance
import good.space.runnershi.model.dto.user.UpdatedUserResponse
import good.space.runnershi.repository.RunningRepository
import good.space.runnershi.service.ServiceLauncher
import good.space.runnershi.state.PauseType
import good.space.runnershi.state.RunningStateManager
import good.space.runnershi.util.CalorieCalculator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

sealed interface RunningUiEvent {
    data class NavigateToResult(
        val userInfo: UpdatedUserResponse,
        val runResult: RunningResultToShow
    ) : RunningUiEvent
    data class RunNotUploadable(
        val runResult: RunningResultToShow
    ) : RunningUiEvent
}

@Serializable
data class RunningResultToShow(
    val distance: Double,
    val runningDurationMillis: Long,
    val totalDurationMillis: Long,
    val runningPace: String,
    val totalPace: String,
    val calory: Int,
    val pacePercentile: String? = null,  // 상위 몇 % (예: "43" = 상위 43%)
    val pathSegments: List<List<LocationModel>> = emptyList()  // 러닝 경로 세그먼트
) {
    // Duration으로 변환하는 헬퍼 프로퍼티
    @OptIn(ExperimentalTime::class)
    val runningDuration: Duration
        get() = runningDurationMillis.toDuration(DurationUnit.MILLISECONDS)
    
    @OptIn(ExperimentalTime::class)
    val totalDuration: Duration
        get() = totalDurationMillis.toDuration(DurationUnit.MILLISECONDS)
}

enum class UploadState {
    IDLE, UPLOADING, SUCCESS, FAILURE
}

@OptIn(ExperimentalTime::class)
class RunningViewModel(
    private val serviceLauncher: ServiceLauncher,
    private val runningRepository: RunningRepository
) : ViewModel() {

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

    val currentPace: StateFlow<String> = totalDistanceMeters
        .map { distance ->
            val currentDuration = durationSeconds.value
            PaceCalculator.calculatePace(distance, currentDuration)
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "-'--''"
        )

    val currentCalories: StateFlow<Int> = totalDistanceMeters
        .map { distance ->
            CalorieCalculator.calculateCalories(distance)
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // 결과 화면 표시 여부 및 데이터
    private val _runResult = MutableStateFlow<RunResult?>(null)
    val runResult: StateFlow<RunResult?> = _runResult.asStateFlow()
    
    // 최대 기록
    private val _personalBest = MutableStateFlow<LongestDistance?>(null)
    val personalBest: StateFlow<LongestDistance?> = _personalBest.asStateFlow()
    
    // 업로드 상태 관리
    private val _uploadState = MutableStateFlow(UploadState.IDLE)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    private val _uiEvent = Channel<RunningUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()
    
    init {
        fetchPersonalBest()
    }
    
    private fun fetchPersonalBest() {
        viewModelScope.launch {
            runningRepository.getPersonalBest()
                .onSuccess { record ->
                    _personalBest.value = record
                }
                .onFailure {
                    // 에러 무시
                }
        }
    }

    fun startRun() {
        if (durationSeconds.value > 0) {
            serviceLauncher.resumeService()
        } else {
            serviceLauncher.startService()
        }
    }

    fun pauseRun() {
        serviceLauncher.pauseService()
    }

    fun stopRun() {
        serviceLauncher.stopService()
    }
    
    // 러닝 종료
    fun finishRun() {
        // 현재 상태 캡처
        val result = createRunResultSnapshot()

        // 서비스 종료 및 초기화
        serviceLauncher.stopService()
        RunningStateManager.reset()

        // 결과 화면으로 이동
        _runResult.value = result

        // 업로드 상태 초기화
        _uploadState.value = UploadState.IDLE

        // 서버 전송 여부 판단
        if (shouldUploadToServer(result)) {
            uploadRunData(result)
        } else {
            // 미달 시 서버 전송 안 함 & DB 정리 (퍼센타일은 조회 가능하므로 조회 시도)
            viewModelScope.launch {
                val percentileResult = runningRepository.getPercentile(
                    distanceMeters = result.totalDistanceMeters,
                    durationSeconds = result.duration.inWholeSeconds
                )
                val percentile = percentileResult.getOrNull()?.topPercent
                
                _uiEvent.send(RunningUiEvent.RunNotUploadable(result.toShow(percentile)))
                onFinishCallback?.invoke()
            }
        }
    }

    fun resetState() {
        RunningStateManager.reset()
    }
    
    private fun shouldUploadToServer(result: RunResult): Boolean {
        return true
//        return result.totalDistanceMeters >= 300.0 && result.duration.inWholeSeconds >= 180
    }

    private fun createRunResultSnapshot(): RunResult {
        val distance = RunningStateManager.totalDistanceMeters.value
        val durationSeconds = RunningStateManager.durationSeconds.value
        val startTime = RunningStateManager.startTime.value
        val finishedAt = Clock.System.now()
        
        val duration = durationSeconds.toDuration(DurationUnit.SECONDS)
        
        val totalTime = if (startTime != null) {
            val calculated = finishedAt - startTime
            if (calculated.isPositive()) calculated else duration
        } else {
            duration
        }
        
        val finalTotalTime = if (totalTime >= duration) {
            totalTime
        } else {
            duration
        }
        
        return RunResult(
            totalDistanceMeters = distance,
            duration = duration,
            totalTime = finalTotalTime,
            pathSegments = RunningStateManager.pathSegments.value,
            calories = currentCalories.value,
            startedAt = startTime ?: finishedAt
        )
    }

    private fun uploadRunData(result: RunResult) {
        viewModelScope.launch {
            _uploadState.value = UploadState.UPLOADING
            
            // 러닝 기록 저장
            val saveResult = runningRepository.saveRun(result)
            
            // 퍼센타일 조회
            val percentileResult = runningRepository.getPercentile(
                distanceMeters = result.totalDistanceMeters,
                durationSeconds = result.duration.inWholeSeconds
            )
            
            val percentile = percentileResult.getOrNull()?.topPercent
            
            saveResult
                .onSuccess { updatedUserResponse ->
                    _uploadState.value = UploadState.SUCCESS
                    _uiEvent.send(
                        RunningUiEvent.NavigateToResult(
                            userInfo = updatedUserResponse,
                            runResult = result.toShow(percentile)
                        )
                    )

                    onFinishCallback?.invoke()
                }
                .onFailure {
                    _uploadState.value = UploadState.FAILURE
                }
        }
    }

    private fun RunResult.toShow(pacePercentile: String? = null): RunningResultToShow {
        return RunningResultToShow(
            distance = totalDistanceMeters,
            runningDurationMillis = duration.inWholeMilliseconds,
            totalDurationMillis = totalTime.inWholeMilliseconds,
            runningPace = PaceCalculator.calculatePace(
                totalDistanceMeters,
                duration.inWholeSeconds
            ),
            totalPace = PaceCalculator.calculatePace(
                totalDistanceMeters,
                totalTime.inWholeSeconds
            ),
            calory = calories,
            pacePercentile = pacePercentile,
            pathSegments = pathSegments
        )
    }
}

