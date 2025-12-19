package good.space.runnershi.state

import good.space.runnershi.model.domain.location.LocationModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant

// 앱 어디서든 접근 가능한 러닝 데이터 저장소
object RunningStateManager {
    // 상태 변수들 (ViewModel에 있던 것들 이동)
    private val _currentLocation = MutableStateFlow<LocationModel?>(null)
    val currentLocation: StateFlow<LocationModel?> = _currentLocation.asStateFlow()

    private val _totalDistanceMeters = MutableStateFlow(0.0)
    val totalDistanceMeters: StateFlow<Double> = _totalDistanceMeters.asStateFlow()

    private val _pathSegments = MutableStateFlow<List<List<LocationModel>>>(emptyList())
    val pathSegments: StateFlow<List<List<LocationModel>>> = _pathSegments.asStateFlow()

    private val _durationSeconds = MutableStateFlow(0L)
    val durationSeconds: StateFlow<Long> = _durationSeconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    // 러닝 시작 시간 (휴식시간 포함한 총 시간 계산용)
    private val _startTime = MutableStateFlow<Instant?>(null)
    val startTime: StateFlow<Instant?> = _startTime.asStateFlow()

    // 일시정지 원인 (오토 퍼즈 기능용)
    private val _pauseType = MutableStateFlow(PauseType.NONE)
    val pauseType: StateFlow<PauseType> = _pauseType.asStateFlow()

    // [New] 차량 감지 경고 횟수 (0부터 시작)
    private val _vehicleWarningCount = MutableStateFlow(0)
    val vehicleWarningCount: StateFlow<Int> = _vehicleWarningCount.asStateFlow()

    // 상태 업데이트 함수들 (Service에서 호출)
    fun updateLocation(location: LocationModel, distanceDelta: Double) {
        _currentLocation.value = location
        _totalDistanceMeters.value += distanceDelta
    }

    fun addPathPoint(point: LocationModel) {
        val currentSegments = _pathSegments.value
        if (currentSegments.isNotEmpty()) {
            val lastIdx = currentSegments.lastIndex
            val newSegments = currentSegments.toMutableList()
            newSegments[lastIdx] = newSegments[lastIdx] + point
            _pathSegments.value = newSegments
        } else {
            _pathSegments.value = listOf(listOf(point))
        }
    }
    
    fun addEmptySegment() {
        _pathSegments.value = _pathSegments.value + listOf(emptyList())
    }

    fun updateDuration(seconds: Long) {
        _durationSeconds.value = seconds
    }

    fun setRunningState(isRunning: Boolean) {
        _isRunning.value = isRunning
    }
    
    fun setPauseType(pauseType: PauseType) {
        _pauseType.value = pauseType
    }
    
    /**
     * 차량 경고 횟수 증가
     */
    fun incrementVehicleWarningCount() {
        _vehicleWarningCount.value += 1
    }
    
    /**
     * 일시정지 (Atomic Update: isRunning과 pauseType을 동시에 변경)
     * UI가 한 번에 완벽한 상태로 그려지도록 보장합니다.
     */
    fun pause(type: PauseType) {
        _isRunning.value = false
        _pauseType.value = type
    }
    
    /**
     * 재개 (Atomic Update: isRunning과 pauseType을 동시에 변경)
     * UI가 한 번에 완벽한 상태로 그려지도록 보장합니다.
     */
    fun resume() {
        _isRunning.value = true
        _pauseType.value = PauseType.NONE
    }
    
    fun reset() {
        _currentLocation.value = null
        _totalDistanceMeters.value = 0.0
        _pathSegments.value = emptyList()
        _durationSeconds.value = 0L
        _isRunning.value = false
        _startTime.value = null
        _pauseType.value = PauseType.NONE
        _vehicleWarningCount.value = 0 // 러닝 시작 시 0으로 리셋
    }
    
    // 러닝 시작 시간 설정 (첫 START 버튼 클릭 시)
    fun setStartTime(instant: Instant) {
        _startTime.value = instant
    }
    
    // [NEW] 복구용 함수: 강제로 값을 세팅
    fun restoreTotalDistance(distance: Double) {
        _totalDistanceMeters.value = distance
    }

    fun restorePathSegments(segments: List<List<LocationModel>>) {
        _pathSegments.value = segments
    }
}

