package good.space.runnershi.database

import android.content.Context
import good.space.runnershi.model.domain.location.LocationModel
import good.space.runnershi.state.RunningStateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import java.util.UUID

class LocalRunningDataSource(context: Context) {
    private val dao = AppDatabase.getDatabase(context).runningDao()
    private var currentRunId: String? = null
    private var currentSegmentIndex: Int = 0
    
    // ============================================
    // 버퍼 기반 벌크 저장 최적화
    // ============================================
    // 메모리 버퍼: 위치 데이터를 임시로 저장
    private val locationBuffer = mutableListOf<LocationEntity>()
    // Thread-Safety를 위한 Mutex (여러 코루틴이 동시 접근해도 안전)
    private val bufferMutex = Mutex()
    // 한 번에 저장할 위치 데이터 개수 (조정 가능: 5~20 권장)
    private val BATCH_SIZE = 10
    
    // 마지막 세션 통계 업데이트 시간 (세션 통계는 자주 업데이트하지 않도록)
    private var lastStatsUpdateTime: Long = 0
    private val STATS_UPDATE_INTERVAL_MS = 5000L // 5초마다 세션 통계 업데이트

    // 1. 러닝 시작 (DB 세션 생성)
    suspend fun startRun() = withContext(Dispatchers.IO) {
        // 기존 세션이 있으면 삭제 (새로운 러닝 시작 전 정리)
        val existingSession = dao.getUnfinishedSession()
        if (existingSession != null) {
            // 기존 미완료 세션 삭제
            dao.deleteSessionById(existingSession.runId)
        }
        
        // 버퍼 초기화 (새 러닝 시작 시)
        bufferMutex.withLock {
            locationBuffer.clear()
        }
        
        val runId = UUID.randomUUID().toString()
        currentRunId = runId
        currentSegmentIndex = 0
        lastStatsUpdateTime = 0 // 통계 업데이트 시간 초기화

        val session = RunSessionEntity(
            runId = runId,
            startTime = System.currentTimeMillis(),
            totalDistance = 0.0,
            durationSeconds = 0,
            isFinished = false
        )
        dao.insertSession(session)
    }

    // 2. 버퍼 기반 데이터 저장 (Service에서 호출)
    // 변경: saveLocation -> bufferLocation (버퍼에 추가, 자동 플러시)
    suspend fun saveLocation(location: LocationModel, totalDistance: Double, durationSeconds: Long) {
        val runId = currentRunId ?: return

        // 세션이 존재하는지 확인 (로그아웃 등으로 삭제되었을 수 있음)
        val session = dao.getUnfinishedSession()
        if (session == null || session.runId != runId) {
            // 세션이 없거나 다른 세션이면 저장하지 않음
            currentRunId = null
            // 버퍼도 비우기
            bufferMutex.withLock {
                locationBuffer.clear()
            }
            return
        }

        // 2-1. 세션 정보 업데이트 (요약 정보) - 자주 업데이트하지 않도록 최적화
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastStatsUpdateTime >= STATS_UPDATE_INTERVAL_MS) {
            withContext(Dispatchers.IO) {
                dao.updateSessionStats(runId, totalDistance, durationSeconds)
            }
            lastStatsUpdateTime = currentTime
        }

        // 2-2. 좌표를 버퍼에 추가 (메모리 연산, 매우 빠름)
        val entity = LocationEntity(
            runSessionId = runId,
            latitude = location.latitude,
            longitude = location.longitude,
            timestamp = location.timestamp,
            segmentIndex = currentSegmentIndex
        )
        
        // 버퍼에 추가하고, 가득 찼으면 자동으로 DB에 저장
        bufferMutex.withLock {
            locationBuffer.add(entity)
            
            // 버퍼가 가득 찼으면 DB에 일괄 저장 (Flush)
            if (locationBuffer.size >= BATCH_SIZE) {
                flushBufferLocked()
            }
        }
    }
    
    /**
     * 버퍼의 데이터를 DB에 일괄 저장 (내부 함수, Mutex 락 내부에서만 호출)
     * 주의: 이 함수는 bufferMutex.withLock 내부에서만 호출해야 함
     */
    private suspend fun flushBufferLocked() {
        if (locationBuffer.isEmpty()) return
        
        // 리스트의 복사본을 만들고 버퍼 비우기 (매우 중요!)
        // 이렇게 하면 DB 저장 중에도 새로운 데이터를 버퍼에 추가할 수 있음
        val locationsToSave = locationBuffer.toList()
        locationBuffer.clear()
        
        // DB 트랜잭션으로 한 번에 저장 (벌크 삽입)
        withContext(Dispatchers.IO) {
            dao.insertLocations(locationsToSave)
        }
        
        android.util.Log.d("LocalRunningDataSource", "💾 Flushed ${locationsToSave.size} locations to DB")
    }
    
    /**
     * 강제 저장: 버퍼에 남은 모든 데이터를 즉시 DB에 저장
     * 러닝 종료 시 반드시 호출해야 함 (데이터 손실 방지)
     */
    suspend fun forceFlush() = withContext(Dispatchers.IO) {
        bufferMutex.withLock {
            flushBufferLocked()
        }
    }

    // 3. 일시정지 후 재개 시 (세그먼트 인덱스 증가)
    fun incrementSegmentIndex() {
        currentSegmentIndex++
    }

    // 4. 러닝 종료 (완료 마킹)
    suspend fun finishRun() = withContext(Dispatchers.IO) {
        // 중요: 종료 전에 버퍼에 남은 모든 데이터를 강제 저장
        forceFlush()
        
        val runId = currentRunId
        runId?.let { dao.finishSession(it) }
        currentRunId = null
        currentSegmentIndex = 0
        
        // 버퍼 초기화
        bufferMutex.withLock {
            locationBuffer.clear()
        }
        // runId는 반환하지 않지만, discardCurrentRun()에서 최신 완료 세션을 삭제할 수 있도록 함
    }

    // 5-1. [감지] 복구할 데이터가 있는지 확인만 하는 함수 (UI 트리거용)
    suspend fun hasUnfinishedRun(): Boolean = withContext(Dispatchers.IO) {
        dao.getUnfinishedSession() != null
    }

    // 5-2. [복구] 실제 복구 수행 (사용자가 "예" 했을 때)
    suspend fun restoreRun(): Boolean = withContext(Dispatchers.IO) {
        val unfinishedSession = dao.getUnfinishedSession() ?: return@withContext false
        val runId = unfinishedSession.runId
        val points = dao.getLocationsBySession(runId)

        if (points.isEmpty()) {
            discardRun() // 데이터가 껍데기만 있으면 삭제
            return@withContext false
        }

        // --- StateManager 복구 로직 (기존과 동일) ---
        currentRunId = runId
        
        RunningStateManager.reset()
        // 시작 시간 복구 (휴식시간 포함한 총 시간 계산용)
        // Room DB는 Long으로 저장하므로 Instant로 변환
        val startTimeInstant = Instant.fromEpochMilliseconds(unfinishedSession.startTime)
        RunningStateManager.setStartTime(startTimeInstant)
        RunningStateManager.setRunningState(false) // PAUSE 상태로 시작
        RunningStateManager.updateDuration(unfinishedSession.durationSeconds)
        RunningStateManager.restoreTotalDistance(unfinishedSession.totalDistance)

        // 경로 재조립
        val segmentsMap = points.groupBy { it.segmentIndex }
        val maxIndex = segmentsMap.keys.maxOrNull() ?: 0
        currentSegmentIndex = maxIndex

        val recoveredSegments = mutableListOf<List<LocationModel>>()
        for (i in 0..maxIndex) {
            val entities = segmentsMap[i] ?: emptyList()
            val models = entities.map { 
                LocationModel(it.latitude, it.longitude, it.timestamp) 
            }
            recoveredSegments.add(models)
        }
        RunningStateManager.restorePathSegments(recoveredSegments)
        
        // 마지막 위치 복구
        val lastPoint = points.last()
        RunningStateManager.updateLocation(
            LocationModel(lastPoint.latitude, lastPoint.longitude, lastPoint.timestamp),
            0.0
        )

        return@withContext true
    }

    // 5-3. [폐기] 복구 거부 시 데이터 삭제 (사용자가 "아니요" 했을 때)
    suspend fun discardRun() = withContext(Dispatchers.IO) {
        // 버퍼 초기화 (삭제 전에 버퍼도 비우기)
        bufferMutex.withLock {
            locationBuffer.clear()
        }
        
        // 먼저 미완료 세션 확인
        val unfinishedSession = dao.getUnfinishedSession()
        if (unfinishedSession != null) {
            // 미완료 세션이 있으면 삭제
            dao.deleteSessionById(unfinishedSession.runId)
        } else {
            // 미완료 세션이 없으면 최신 완료 세션 삭제 (러닝 종료 후 호출되는 경우)
            // 최신 세션(완료 포함)을 가져와서 삭제
            val latestSession = dao.getLatestSession()
            latestSession?.let {
                dao.deleteSessionById(it.runId)
            }
        }
        currentRunId = null
        currentSegmentIndex = 0
    }
    
    // 6. [로그아웃용] 모든 러닝 데이터 삭제 (완료된 세션 포함)
    suspend fun discardAllRuns() = withContext(Dispatchers.IO) {
        // 버퍼 초기화
        bufferMutex.withLock {
            locationBuffer.clear()
        }
        
        // 모든 세션 삭제 (CASCADE로 좌표도 자동 삭제됨)
        dao.deleteAllSessions()
        currentRunId = null
        currentSegmentIndex = 0
    }
}

