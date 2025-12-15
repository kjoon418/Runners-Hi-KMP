package good.space.runnershi.viewmodel

import good.space.runnershi.location.LocationTracker
import good.space.runnershi.model.domain.LocationModel
import good.space.runnershi.util.DistanceCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RunningViewModel(
    private val locationTracker: LocationTracker
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    // 1. 현재 위치
    private val _currentLocation = MutableStateFlow<LocationModel?>(null)
    val currentLocation: StateFlow<LocationModel?> = _currentLocation.asStateFlow()

    // 2. 총 뛴 거리 (미터)
    private val _totalDistanceMeters = MutableStateFlow(0.0)
    val totalDistanceMeters: StateFlow<Double> = _totalDistanceMeters.asStateFlow()

    // 이전 위치를 기억하기 위한 변수
    private var lastLocation: LocationModel? = null

    // GPS 노이즈 필터링 임계값 (예: 2미터 미만 이동은 무시)
    private val MIN_DISTANCE_THRESHOLD = 2.0

    fun startRun() {
        scope.launch {
            locationTracker.startTracking().collect { newLocation ->
                updateRunData(newLocation)
            }
        }
    }

    private fun updateRunData(newLocation: LocationModel) {
        val lastLoc = lastLocation

        if (lastLoc != null) {
            // 거리 계산 수행
            val distanceDelta = DistanceCalculator.calculateDistance(lastLoc, newLocation)

            // 노이즈 필터링: 의미 있는 거리만큼 이동했는지 확인
            if (distanceDelta >= MIN_DISTANCE_THRESHOLD) {
                _totalDistanceMeters.value += distanceDelta
                lastLocation = newLocation // 유효한 이동일 때만 갱신
                _currentLocation.value = newLocation
            }
        } else {
            // 첫 위치 수신 시
            lastLocation = newLocation
            _currentLocation.value = newLocation
        }
    }
    
    fun stopRun() {
        locationTracker.stopTracking()
        // 필요시 DB 저장 로직 호출
    }
}

