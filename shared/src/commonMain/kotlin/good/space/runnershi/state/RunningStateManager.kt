package good.space.runnershi.state

import good.space.runnershi.model.domain.LocationModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    
    fun reset() {
        _currentLocation.value = null
        _totalDistanceMeters.value = 0.0
        _pathSegments.value = emptyList()
        _durationSeconds.value = 0L
        _isRunning.value = false
    }
    
    // [NEW] 복구용 함수: 강제로 값을 세팅
    fun restoreTotalDistance(distance: Double) {
        _totalDistanceMeters.value = distance
    }

    fun restorePathSegments(segments: List<List<LocationModel>>) {
        _pathSegments.value = segments
    }
}

