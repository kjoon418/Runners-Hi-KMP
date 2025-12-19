package good.space.runnershi.model.domain.running

import good.space.runnershi.model.domain.location.LocationModel
import kotlinx.datetime.Clock

/**
 * 사용자의 이동 상태를 분석한 결과를 담는다.
 *
 * @property status 현재 상태
 * @property isStatusChanged 상태가 변경되었는지 여부
 * @property statusDurationMs 현재 상태가 지속된 시간
 */
data class MovementAnalysisResult(
    val status: MovementStatus,
    val isStatusChanged: Boolean,
    val statusDurationMs: Long = 0
)

enum class MovementStatus {
    STOPPED,    // 정지 (휴식 중)
    MOVING,     // 러닝 (정상 운동 중)
    VEHICLE     // 과속 (차량 이동 의심)
}

/**
 * 사용자의 위치 정보를 기반으로, 현재 사용자가 이동 중인지 정지 중인지 판단한다.
 *
 * @property stopThreshold 정지 상태로 간주할 속도 기준 (m/s).
 * @property moveThreshold 이동 상태로 간주할 속도 기준 (m/s).
 * @property vehicleThreshold 차량 탑승 상태로 간주할 속도 기준 (m/s).
 *
 * @property stopDurationMs 정지 상태로 확정하기 위해 필요한 지속 시간 (ms).
 * @property moveDurationMs 이동 상태로 확정하기 위해 필요한 지속 시간 (ms).
 * @property vehicleDurationMs 차량 탑승 상태로 확정하기 위해 필요한 지속 시간 (ms).
 *
 * @property clock 시간 측정을 위한 Clock 인스턴스 (테스트 시 주입 가능).
 */
class MovementAnalyzer(
    private val stopThreshold: Float = 1.2f,
    private val moveThreshold: Float = 1.8f,
    private val vehicleThreshold: Float = 8.33f,

    private val stopDurationMs: Long = 3000L,
    private val moveDurationMs: Long = 2000L,
    private val vehicleDurationMs: Long = 5000L,

    private val clock: Clock = Clock.System
) {
    // 상태별 타이머
    private var stopSince: Long? = null
    private var moveSince: Long? = null
    private var vehicleSince: Long? = null

    // 현재 상태
    private var currentStatus = MovementStatus.STOPPED

    // 현재 상태가 확정된 시점
    private var statusConfirmedAt: Long = now

    // 현재 시간
    private val now: Long
        get() = clock.now().toEpochMilliseconds()

    /**
     * 현재 시간부터 측정하도록 설정한다.
     */
    fun start(
        initialStatus: MovementStatus = MovementStatus.STOPPED
    ) {
        currentStatus = initialStatus
        statusConfirmedAt = now
        resetTimers()
    }

    /**
     * 속도 정보를 기반으로 이동 상태를 분석한다
     */
    fun analyze(
        location: LocationModel
    ): MovementAnalysisResult {
        val speed = location.speed
        val accuracy = location.accuracy
        val currentTime = this.now

        // 과속 감지
        if (isVehicle(speed, accuracy)) {
            resetTimers(except = MovementStatus.VEHICLE)
            updateVehicleSince(currentTime)

            if (isStatusChanged(
                    newStatus = MovementStatus.VEHICLE,
                    timeStart = vehicleSince!!,
                    durationLimit = vehicleDurationMs,
                    currentTime = currentTime
                )
            ) {
                updateStatus(MovementStatus.VEHICLE, currentTime)

                return MovementAnalysisResult(
                    status = MovementStatus.VEHICLE,
                    isStatusChanged = true
                )
            }
        }
        // 정지 감지
        else if (isStopped(speed)) {
            resetTimers(except = MovementStatus.STOPPED)
            updateStopSince(currentTime)

            if (isStatusChanged(
                    newStatus = MovementStatus.STOPPED,
                    timeStart = stopSince!!,
                    durationLimit = stopDurationMs,
                    currentTime = currentTime
                )
            ) {
                updateStatus(MovementStatus.STOPPED, currentTime)

                return MovementAnalysisResult(
                    status = MovementStatus.STOPPED,
                    isStatusChanged = true
                )
            }
        }
        // 이동 감지
        else if (isMoving(speed)) {
            resetTimers(except = MovementStatus.MOVING)
            updateMoveSince(currentTime)

            if (isStatusChanged(
                    newStatus = MovementStatus.MOVING,
                    timeStart = moveSince!!,
                    durationLimit = moveDurationMs,
                    currentTime = currentTime
                )
            ) {
                updateStatus(MovementStatus.MOVING, currentTime)

                return MovementAnalysisResult(
                    status = MovementStatus.MOVING,
                    isStatusChanged = true
                )
            }
        }
        // 속도가 보류 구간에 있는 경우(정지와 이동 사이)
        else if (isDeadZone(speed)) {
            resetTimers()
        }

        // 변화 없음
        return MovementAnalysisResult(
            status = currentStatus,
            isStatusChanged = false,
            statusDurationMs = currentTime - statusConfirmedAt
        )
    }

    private fun isVehicle(
        speed: Float,
        accuracy: Float
    ): Boolean {
        return speed >= vehicleThreshold && accuracy < 20.0f
    }

    private fun isStopped(
        speed: Float
    ): Boolean {
        return speed < stopThreshold
    }

    private fun isMoving(
        speed: Float
    ): Boolean {
        return speed > moveThreshold
    }

    private fun isDeadZone(
        speed: Float
    ): Boolean {
        return speed in stopThreshold..moveThreshold
    }

    private fun isStatusChanged(
        newStatus: MovementStatus,
        timeStart: Long,
        durationLimit: Long,
        currentTime: Long
    ): Boolean {
        if (currentStatus == newStatus) {
            return false
        }

        val elapsed = currentTime - timeStart

        return elapsed >= durationLimit
    }

    private fun updateStatus(
        newStatus: MovementStatus,
        confirmedAt: Long
    ) {
        currentStatus = newStatus
        statusConfirmedAt = confirmedAt
    }

    private fun resetTimers(except: MovementStatus? = null) {
        if (except != MovementStatus.STOPPED) {
            stopSince = null
        }
        if (except != MovementStatus.MOVING) {
            moveSince = null
        }
        if (except != MovementStatus.VEHICLE) {
            vehicleSince = null
        }
    }

    private fun updateVehicleSince(time: Long) {
        if (vehicleSince != null) {
            return
        }

        vehicleSince = time
    }

    private fun updateMoveSince(time: Long) {
        if (moveSince != null) {
            return
        }

        moveSince = time
    }

    private fun updateStopSince(time: Long) {
        if (stopSince != null) {
            return
        }

        stopSince = time
    }
}
