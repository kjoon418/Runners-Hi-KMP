package good.space.runnershi.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import good.space.runnershi.MainActivity
import good.space.runnershi.database.LocalRunningDataSource
import good.space.runnershi.location.AndroidLocationTracker
import good.space.runnershi.model.domain.location.LocationModel
import good.space.runnershi.model.domain.location.MovementAnalyzer
import good.space.runnershi.model.domain.location.MovementStatus
import good.space.runnershi.settings.AndroidSettingsRepository
import good.space.runnershi.state.PauseType
import good.space.runnershi.state.RunningStateManager
import good.space.runnershi.util.DistanceCalculator
import good.space.runnershi.util.TimeFormatter
import good.space.runnershi.util.format
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class RunningService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var locationTracker: AndroidLocationTracker
    private lateinit var dbSource: LocalRunningDataSource
    private lateinit var settingsRepository: AndroidSettingsRepository
    private var lastLocation: LocationModel? = null

    // 이동 상태 분석기
    private val movementAnalyzer = MovementAnalyzer()

    // 액션 상수 정의
    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        
        const val CHANNEL_ID = "running_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        locationTracker = AndroidLocationTracker(this)
        dbSource = LocalRunningDataSource(this)
        settingsRepository = AndroidSettingsRepository(this)
        createNotificationChannel()
    }

    // 서비스가 시작될 때 호출됨 (startService 호출 시)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRunning()
            ACTION_PAUSE -> pauseRunning()
            ACTION_RESUME -> resumeRunning()
            ACTION_STOP -> stopRunning()
        }
        return START_STICKY // 시스템에 의해 죽어도 다시 살아남
    }

    private fun startRunning() {
        RunningStateManager.reset()
        // 러닝 시작 시간 기록 (휴식시간 포함한 총 시간 계산용)
        RunningStateManager.setStartTime(Clock.System.now())
        RunningStateManager.setRunningState(true)
        RunningStateManager.addEmptySegment()

        // 분석기 초기화
        movementAnalyzer.start(initialStatus = MovementStatus.MOVING)

        // 0. DB 세션 시작
        serviceScope.launch {
            dbSource.startRun()
        }

        // 1. Foreground 알림 시작 (필수!)
        startForeground(NOTIFICATION_ID, buildNotification("00:00", "0.00 km"))

        // 2. 타이머 시작
        startTimer()

        // 3. 위치 추적 시작
        startLocationTracking()
    }
    
    private fun resumeRunning() {
        // Atomic Update: isRunning과 pauseType을 동시에 변경
        RunningStateManager.resume()
        RunningStateManager.addEmptySegment() // 끊긴 구간 처리
        dbSource.incrementSegmentIndex() // DB 세그먼트 인덱스 증가
        lastLocation = null // 순간이동 방지
        
        // [핵심] 분석기 초기화: "지금부터 달리는 상태로 분석 시작해!"
        // 이렇게 하면 재개 직후 2초간 굼뜨는 현상을 막을 수 있습니다.
        movementAnalyzer.start(initialStatus = MovementStatus.MOVING)
        
        // Foreground 알림 다시 시작
        startForeground(NOTIFICATION_ID, buildNotification(
            TimeFormatter.formatSecondsToTime(RunningStateManager.durationSeconds.value),
            calculateDistanceString()
        ))
        
        // 타이머 재시작
        startTimer()
        
        // 위치 추적 시작
        startLocationTracking()
    }

    private fun pauseRunning() {
        // Atomic Update: 사용자가 수동으로 일시정지
        RunningStateManager.pause(PauseType.USER_PAUSE)
        // 알림 업데이트 (PAUSED 표시)
        updateNotification("PAUSED", calculateDistanceString())
    }
    
    /**
     * 과속 감지 알림 생성
     */
    private fun buildOverSpeedNotification(): android.app.Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⚠️ 이동 속도가 너무 빠릅니다")
            .setContentText("차량 탑승이 감지되어 일시정지합니다.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 높은 우선순위로 설정
            .build()
    }

    private fun stopRunning() {
        RunningStateManager.setRunningState(false)
        stopLocationTracking()
        timerJob?.cancel()
        
        // DB 세션 종료 마킹 및 삭제 (서버 저장 성공 또는 기록 미달 시 즉시 삭제)
        // 주의: 서버 업로드 실패 시에는 재전송을 위해 데이터를 유지해야 하지만,
        // 현재는 finishRun()에서 완료 마킹 후 즉시 삭제하도록 변경
        // (서버 업로드는 RunningViewModel에서 처리되므로, 여기서는 완료 마킹만 하고 삭제는 ViewModel 콜백에서 처리)
        serviceScope.launch {
            dbSource.finishRun()
            // 완료 마킹 후 즉시 삭제 (앱 강제 종료 시에도 데이터가 남지 않도록)
            // 서버 업로드는 이미 완료되었거나 기록 미달이므로 삭제해도 안전
            dbSource.discardRun()
        }
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf() // 서비스 종료
    }

    private var timerJob: Job? = null
    private var trackingJob: Job? = null

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive && RunningStateManager.isRunning.value) {
                delay(1000L)
                val currentSec = RunningStateManager.durationSeconds.value + 1
                RunningStateManager.updateDuration(currentSec)
                
                // 알림창 텍스트 갱신 (1초마다)
                updateNotification(
                    TimeFormatter.formatSecondsToTime(currentSec),
                    calculateDistanceString()
                )
            }
        }
    }


    private fun startLocationTracking() {
        trackingJob?.cancel()
        trackingJob = locationTracker.startTracking()
            .onEach { location ->
                // 1. 분석기에게 판단 위임
                val analysisResult = movementAnalyzer.analyze(location)

                // 2. 상태 변화가 있을 때만 반응
                if (analysisResult.isStatusChanged) {
                    handleStatusChange(analysisResult.status)
                }

                // 3. '달리는 중'이고 'MOVING' 상태일 때만 거리 계산 및 DB 저장
                if (RunningStateManager.isRunning.value && 
                    analysisResult.status == MovementStatus.MOVING) {
                    processRunningLocation(location)
                } else {
                    // PAUSE 상태이거나 MOVING이 아닐 때는 위치만 갱신
                    lastLocation = location
                    RunningStateManager.updateLocation(location, 0.0)
                }
            }.launchIn(serviceScope)
    }
    
    /**
     * 상태 변화 처리 핸들러
     */
    private fun handleStatusChange(newStatus: MovementStatus) {
        when (newStatus) {
            MovementStatus.VEHICLE -> {
                // 1. 경고 횟수를 1 올립니다.
                RunningStateManager.incrementVehicleWarningCount()
                val currentCount = RunningStateManager.vehicleWarningCount.value
                
                android.util.Log.w("RunningService", "🚨 과속 감지! 누적 횟수: $currentCount")

                // 2. 횟수에 따라 처분을 결정합니다.
                if (currentCount >= 2) {
                    // [2회 이상] 아웃! -> 강제 종료 로직 실행
                    handleForcedFinishByVehicle()
                } else {
                    // [1회차] 경고! -> 일시정지하고 기회 줌
                    performAutoPause(PauseType.AUTO_PAUSE_VEHICLE)
                }
            }
            MovementStatus.STOPPED -> {
                // (기존 동일) 동기적으로 설정 확인
                if (settingsRepository.isAutoPauseEnabledSync()) {
                    performAutoPause(PauseType.AUTO_PAUSE_REST)
                }
            }
            MovementStatus.MOVING -> {
                // (기존 동일) 자동 재개 로직
                val pauseType = RunningStateManager.pauseType.value
                if (!RunningStateManager.isRunning.value && 
                    pauseType == PauseType.AUTO_PAUSE_REST) {
                    performAutoResume()
                }
            }
        }
    }
    
    /**
     * 자동 일시정지 수행
     */
    private fun performAutoPause(pauseType: PauseType) {
        when (pauseType) {
            PauseType.AUTO_PAUSE_VEHICLE -> {
                // 과속 감지: 경고 알림 표시
                RunningStateManager.pause(pauseType)
                val notification = buildOverSpeedNotification()
                startForeground(NOTIFICATION_ID, notification)
            }
            PauseType.AUTO_PAUSE_REST -> {
                // 휴식 감지: 조용히 일시정지
                RunningStateManager.pause(pauseType)
                updateNotification("휴식 중", calculateDistanceString())
            }
            else -> {
                // 기타: 일반 일시정지
                RunningStateManager.pause(pauseType)
            }
        }
    }
    
    /**
     * 자동 재개 수행 (휴식에서 이동으로 전환 시)
     */
    private fun performAutoResume() {
        RunningStateManager.resume()
        RunningStateManager.addEmptySegment()
        dbSource.incrementSegmentIndex()
        lastLocation = null
        
        // 분석기 초기화
        movementAnalyzer.start(initialStatus = MovementStatus.MOVING)
        
        // 알림 업데이트
        updateNotification(
            TimeFormatter.formatSecondsToTime(RunningStateManager.durationSeconds.value),
            calculateDistanceString()
        )
        
        // 타이머 재시작
        startTimer()
    }
    
    /**
     * 달리는 중일 때 위치 데이터 처리 (거리 계산 및 DB 저장)
     */
    private fun processRunningLocation(location: LocationModel) {
        val lastLoc = lastLocation

        if (lastLoc != null) {
            val dist = DistanceCalculator.calculateDistance(lastLoc, location)
            // 2m 이상 이동했을 때만 기록
            if (dist >= 2.0) {
                RunningStateManager.updateLocation(location, dist)
                RunningStateManager.addPathPoint(location)
                lastLocation = location
                
                val totalDist = RunningStateManager.totalDistanceMeters.value
                val duration = RunningStateManager.durationSeconds.value
                
                serviceScope.launch {
                    dbSource.saveLocation(location, totalDist, duration)
                }
            }
        } else {
            // 첫 위치 기록
            lastLocation = location
            RunningStateManager.updateLocation(location, 0.0)
            RunningStateManager.addPathPoint(location)
            
            serviceScope.launch {
                dbSource.saveLocation(location, 0.0, RunningStateManager.durationSeconds.value)
            }
        }
    }

    private fun stopLocationTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }

    // --- Notification Helpers ---

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Running Tracker",
                NotificationManager.IMPORTANCE_DEFAULT // 과속 알림을 위해 DEFAULT로 변경
            )
            channel.description = "러닝 추적 및 과속 감지 알림"
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(time: String, distance: String): android.app.Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Runner's Hi - 러닝 중 🏃")
            .setContentText("시간: $time | 거리: $distance")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // 임시 아이콘
            .setContentIntent(pendingIntent)
            .setOngoing(true) // 사용자가 지울 수 없음
            .build()
    }

    private fun updateNotification(time: String, distance: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(time, distance))
    }
    
    /**
     * 제목과 내용을 지정하여 알림을 업데이트하는 함수
     */
    private fun updateNotificationWithTitle(title: String, content: String) {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
            
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    /**
     * [New] 강제 종료 헬퍼 함수
     */
    private fun handleForcedFinishByVehicle() {
        android.util.Log.e("RunningService", "🚨 차량 감지 2회 누적! 러닝을 강제 종료합니다.")
        
        // 1. 상태를 '차량 감지 일시정지'로 변경 
        // (서비스가 직접 종료하지 않고, UI가 이 상태를 보고 종료 절차를 밟게 유도함)
        RunningStateManager.pause(PauseType.AUTO_PAUSE_VEHICLE)

        // 2. 알림 내용을 '강제 종료'로 변경
        updateNotificationWithTitle(
            "러닝 강제 종료", 
            "반복된 차량 이동이 감지되어 기록을 종료합니다."
        )
        
        // 3. 더 이상 위치 추적 불필요 (배터리 절약)
        stopLocationTracking()
        timerJob?.cancel()
    }
    
    private fun calculateDistanceString(): String {
        val dist = RunningStateManager.totalDistanceMeters.value
        return "%.2f km".format(dist / 1000.0)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

