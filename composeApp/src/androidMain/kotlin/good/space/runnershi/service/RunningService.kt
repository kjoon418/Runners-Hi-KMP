package good.space.runnershi.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import good.space.runnershi.MainActivity
import good.space.runnershi.database.LocalRunningDataSource
import good.space.runnershi.location.AndroidLocationTracker
import good.space.runnershi.model.domain.LocationModel
import good.space.runnershi.state.RunningStateManager
import good.space.runnershi.util.DistanceCalculator
import good.space.runnershi.util.TimeFormatter
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

class RunningService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var locationTracker: AndroidLocationTracker
    private lateinit var dbSource: LocalRunningDataSource
    private var lastLocation: LocationModel? = null

    // 의심스러운 좌표들을 잠시 가둬두는 감옥 (버퍼)
    private val suspiciousBuffer = mutableListOf<LocationModel>()

    // 시간 기반 과속 감지용 변수
    private var firstOverSpeedTimestamp: Long? = null
    private val OVER_SPEED_THRESHOLD_MS = 8.33f // 30km/h ≈ 8.33m/s
    // 5초 (GPS 튐 방지 및 차량 탑승 확정 기준)
    private val OVER_SPEED_DURATION_MS = 5000L

    // 액션 상수 정의
    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_OVER_SPEED_DETECTED = "ACTION_OVER_SPEED_DETECTED"
        
        const val CHANNEL_ID = "running_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        locationTracker = AndroidLocationTracker(this)
        dbSource = LocalRunningDataSource(this)
        createNotificationChannel()
        
        // ❌ [삭제] 자동 복구 로직 제거
        // 사용자가 MainActivity에서 다이얼로그를 통해 복구를 선택할 때만 복구됨
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
        RunningStateManager.setRunningState(true)
        RunningStateManager.addEmptySegment() // 첫 세그먼트

        // 버퍼 및 과속 타이머 리셋
        suspiciousBuffer.clear()
        firstOverSpeedTimestamp = null

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
        RunningStateManager.setRunningState(true)
        RunningStateManager.addEmptySegment() // 끊긴 구간 처리
        dbSource.incrementSegmentIndex() // DB 세그먼트 인덱스 증가
        lastLocation = null // 순간이동 방지
        
        // 버퍼 및 과속 타이머 리셋
        suspiciousBuffer.clear()
        firstOverSpeedTimestamp = null
        
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
        RunningStateManager.setRunningState(false)
        // 알림 업데이트 (PAUSED 표시)
        updateNotification("PAUSED", calculateDistanceString())
    }
    
    /**
     * 과속으로 인한 일시정지 (알림 메시지 포함)
     */
    private fun pauseRunningWithOverSpeedNotification() {
        RunningStateManager.setRunningState(false)
        
        // Foreground Service이므로 startForeground를 사용해야 알림이 표시됨
        val notification = buildOverSpeedNotification()
        startForeground(NOTIFICATION_ID, notification)
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

    /**
     * [공통 함수] 유효한 위치 데이터를 처리(거리 계산, State갱신, DB저장)합니다.
     */
    private fun processValidLocation(location: LocationModel) {
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

    private fun startLocationTracking() {
        trackingJob?.cancel()
        trackingJob = locationTracker.startTracking()
            .onEach { newLocation ->
                val running = RunningStateManager.isRunning.value
                
                // 1. PAUSE 상태일 때는 위치만 갱신하고 종료
                if (!running) {
                    lastLocation = newLocation
                    RunningStateManager.updateLocation(newLocation, 0.0)
                    return@onEach
                }

                // ----------------------------------------------------
                // 의심 구간 버퍼링 전략 (Suspicious Buffering)
                // ----------------------------------------------------
                
                // [Case A] 과속 의심 상황 (30km/h 초과)
                if (newLocation.speed > OVER_SPEED_THRESHOLD_MS) {
                    // 1. 즉시 저장하지 않고 버퍼에 "감금"
                    suspiciousBuffer.add(newLocation)
                    
                    // 2. 지도 위치는 업데이트 (사용자가 자신의 위치를 볼 수 있도록)
                    // 단, 거리 계산은 하지 않음 (distanceDelta = 0.0)
                    RunningStateManager.updateLocation(newLocation, 0.0)
                    lastLocation = newLocation
                    
                    // 3. 시간 측정 시작 (최초 감지 시)
                    if (firstOverSpeedTimestamp == null) {
                        firstOverSpeedTimestamp = SystemClock.elapsedRealtime()
                        android.util.Log.d("RunningService", "⚠️ 과속 의심! 버퍼링 시작")
                    }
                    
                    // 4. 지속 시간 체크
                    val duration = SystemClock.elapsedRealtime() - firstOverSpeedTimestamp!!
                    
                    if (duration >= OVER_SPEED_DURATION_MS) {
                        // 유죄 확정: 5초 이상 지속됨 -> 진짜 차를 탄 것임
                        handleVehicleDetected() 
                    }
                    
                    // 버퍼링 중이므로 이번 데이터는 경로에 추가하지 않고 리턴
                    return@onEach 
                } 
                
                // [Case B] 정상 속도 상황 (30km/h 이하)
                else {
                    // 1. 억울하게 갇혀있던 데이터가 있는가? (GPS 튐 현상 종료)
                    if (suspiciousBuffer.isNotEmpty()) {
                        android.util.Log.d("RunningService", "✅ GPS 튐 판정: 버퍼 데이터 ${suspiciousBuffer.size}개 복구")
                        
                        // 버퍼에 있던 데이터들을 순서대로 저장 (Flush)
                        suspiciousBuffer.forEach { bufferedLoc ->
                            processValidLocation(bufferedLoc)
                        }
                        suspiciousBuffer.clear()
                    }
                    
                    // 2. 감지 변수 초기화
                    firstOverSpeedTimestamp = null
                    
                    // 3. 현재 위치 정상 저장
                    processValidLocation(newLocation)
                }
                
            }.launchIn(serviceScope)
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
    
    private fun calculateDistanceString(): String {
        val dist = RunningStateManager.totalDistanceMeters.value
        return String.format("%.2f km", dist / 1000.0)
    }
    
    /**
     * 차량 탑승 확정 시 처리 로직
     * 
     * 5초 이상 과속 상태가 지속되면 차량 탑승으로 판단하고,
     * 버퍼에 있던 의심 데이터를 모두 폐기합니다.
     */
    private fun handleVehicleDetected() {
        android.util.Log.w("RunningService", "🚨 차량 탑승 확정! 버퍼 데이터 폐기 및 일시정지")
        
        // 버퍼에 있던 5초간의 데이터(약 40~50m)를 모두 폐기처분 (Clear)
        suspiciousBuffer.clear()
        firstOverSpeedTimestamp = null
        
        // 일시정지 및 알림
        pauseRunningWithOverSpeedNotification()
        sendOverSpeedBroadcast()
    }
    
    /**
     * 과속 감지 이벤트를 Broadcast로 전송
     */
    private fun sendOverSpeedBroadcast() {
        val intent = Intent(ACTION_OVER_SPEED_DETECTED).apply {
            setPackage(packageName) // 내 앱에게만 보내도록 명시
        }
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

