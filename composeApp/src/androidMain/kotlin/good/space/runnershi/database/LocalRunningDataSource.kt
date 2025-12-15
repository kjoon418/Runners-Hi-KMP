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
    suspend fun startRun() {
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
        currentRunId?.let { dao.finishSession(it) }
        currentRunId = null
        currentSegmentIndex = 0
    }

    // 5. [핵심] 비정상 종료 복구 (앱 켤 때 호출)
    suspend fun recoverLastRunIfAny(): Boolean = withContext(Dispatchers.IO) {
        // 끝나지 않은 세션이 있는지 확인
        val unfinishedSession = dao.getUnfinishedSession() ?: return@withContext false

        val runId = unfinishedSession.runId
        val points = dao.getLocationsBySession(runId)

        if (points.isEmpty()) return@withContext false

        // --- 메모리(StateManager)로 데이터 복구 ---
        currentRunId = runId
        
        // 1. 요약 정보 복구
        RunningStateManager.reset()
        RunningStateManager.setRunningState(false) // 일단 PAUSE 상태로 복구
        RunningStateManager.updateDuration(unfinishedSession.durationSeconds)
        RunningStateManager.restoreTotalDistance(unfinishedSession.totalDistance)

        // 2. 경로 복구 (Multi-Segment 구조 재조립)
        // segmentIndex별로 그룹화: { 0: [Loc, Loc...], 1: [Loc...] }
        val segmentsMap = points.groupBy { it.segmentIndex }
        
        // 정렬된 리스트로 변환
        val maxIndex = segmentsMap.keys.maxOrNull() ?: 0
        currentSegmentIndex = maxIndex // 마지막 세그먼트 인덱스 기억

        // StateManager의 경로 리스트 재구성
        val recoveredSegments = mutableListOf<List<LocationModel>>()
        for (i in 0..maxIndex) {
            val entities = segmentsMap[i] ?: emptyList()
            val models = entities.map { 
                LocationModel(it.latitude, it.longitude, it.timestamp) // altitude 제거됨
            }
            recoveredSegments.add(models)
        }
        
        RunningStateManager.restorePathSegments(recoveredSegments)
        
        // 마지막 위치 설정
        val lastPoint = points.last()
        RunningStateManager.updateLocation(
            LocationModel(lastPoint.latitude, lastPoint.longitude, lastPoint.timestamp), // altitude 제거됨
            0.0
        )

        return@withContext true
    }
}

