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
        createNotificationChannel()
        
        // [중요] 앱이 죽었다 살아나서 서비스가 재시작된 경우 복구 시도
        serviceScope.launch {
            if (dbSource.recoverLastRunIfAny()) {
                // 복구 성공 시 알림 띄우기 (PAUSE 상태로)
                updateNotification(
                    TimeFormatter.formatSecondsToTime(RunningStateManager.durationSeconds.value),
                    String.format("%.2f km", RunningStateManager.totalDistanceMeters.value / 1000.0)
                )
                // 필요하다면 여기서 ViewModel이나 UI에 "복구됨" 이벤트를 보낼 수도 있음
            }
        }
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
        locationTracker.stopTracking()
        // 알림 업데이트 (PAUSED 표시)
        updateNotification("PAUSED", calculateDistanceString())
    }

    private fun stopRunning() {
        RunningStateManager.setRunningState(false)
        locationTracker.stopTracking()
        
        // DB 세션 종료 마킹
        serviceScope.launch {
            dbSource.finishRun()
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
            .onEach { newLocation ->
                // ViewModel에 있던 로직 그대로 적용
                val lastLoc = lastLocation
                if (lastLoc != null) {
                    val dist = DistanceCalculator.calculateDistance(lastLoc, newLocation)
                    if (dist >= 2.0) {
                        RunningStateManager.updateLocation(newLocation, dist)
                        RunningStateManager.addPathPoint(newLocation)
                        lastLocation = newLocation
                        
                        // DB에 저장
                        val totalDist = RunningStateManager.totalDistanceMeters.value
                        val duration = RunningStateManager.durationSeconds.value
                        
                        serviceScope.launch {
                            dbSource.saveLocation(newLocation, totalDist, duration)
                        }
                    }
                } else {
                    lastLocation = newLocation
                    RunningStateManager.updateLocation(newLocation, 0.0)
                    RunningStateManager.addPathPoint(newLocation)
                    
                    // 첫 위치 저장
                    serviceScope.launch {
                        dbSource.saveLocation(newLocation, 0.0, RunningStateManager.durationSeconds.value)
                    }
                }
            }.launchIn(serviceScope)
    }

    // --- Notification Helpers ---

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Running Tracker",
                NotificationManager.IMPORTANCE_LOW // 소리 안 나게 (LOW)
            )
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
    
    private fun calculateDistanceString(): String {
        val dist = RunningStateManager.totalDistanceMeters.value
        return String.format("%.2f km", dist / 1000.0)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

