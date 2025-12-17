package good.space.runnershi.database

import android.content.Context
import good.space.runnershi.model.domain.LocationModel
import good.space.runnershi.state.RunningStateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class LocalRunningDataSource(context: Context) {
    private val dao = AppDatabase.getDatabase(context).runningDao()
    private var currentRunId: String? = null
    private var currentSegmentIndex: Int = 0

    // 1. 러닝 시작 (DB 세션 생성)
    suspend fun startRun() = withContext(Dispatchers.IO) {
        // 기존 세션이 있으면 삭제 (새로운 러닝 시작 전 정리)
        val existingSession = dao.getUnfinishedSession()
        if (existingSession != null) {
            // 기존 미완료 세션 삭제
            dao.deleteSessionById(existingSession.runId)
        }
        
        val runId = UUID.randomUUID().toString()
        currentRunId = runId
        currentSegmentIndex = 0

        val session = RunSessionEntity(
            runId = runId,
            startTime = System.currentTimeMillis(),
            totalDistance = 0.0,
            durationSeconds = 0,
            isFinished = false
        )
        dao.insertSession(session)
    }

    // 2. 실시간 데이터 저장 (Service에서 호출)
    suspend fun saveLocation(location: LocationModel, totalDistance: Double, durationSeconds: Long) {
        val runId = currentRunId ?: return

        // 세션이 존재하는지 확인 (로그아웃 등으로 삭제되었을 수 있음)
        val session = dao.getUnfinishedSession()
        if (session == null || session.runId != runId) {
            // 세션이 없거나 다른 세션이면 저장하지 않음
            currentRunId = null
            return
        }

        // 2-1. 세션 정보 업데이트 (요약 정보)
        dao.updateSessionStats(runId, totalDistance, durationSeconds)

        // 2-2. 좌표 저장 (상세 정보)
        val entity = LocationEntity(
            runSessionId = runId,
            latitude = location.latitude,
            longitude = location.longitude,
            // altitude = location.altitude, // [삭제] 일반 러닝 앱에서는 불필요
            timestamp = location.timestamp,
            segmentIndex = currentSegmentIndex
        )
        dao.insertLocation(entity)
    }

    // 3. 일시정지 후 재개 시 (세그먼트 인덱스 증가)
    fun incrementSegmentIndex() {
        currentSegmentIndex++
    }

    // 4. 러닝 종료 (완료 마킹)
    suspend fun finishRun() {
        val runId = currentRunId
        runId?.let { dao.finishSession(it) }
        currentRunId = null
        currentSegmentIndex = 0
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
        // 모든 세션 삭제 (CASCADE로 좌표도 자동 삭제됨)
        dao.deleteAllSessions()
        currentRunId = null
        currentSegmentIndex = 0
    }
}

