package good.space.runnershi.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Dao
interface RunningDao {
    // 세션 생성
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: RunSessionEntity)

    // 세션 업데이트 (거리/시간 갱신)
    @Query("UPDATE run_sessions SET totalDistance = :distance, durationSeconds = :seconds WHERE runId = :runId")
    suspend fun updateSessionStats(runId: String, distance: Double, seconds: Long)

    // 러닝 종료 처리
    @Query("UPDATE run_sessions SET isFinished = 1 WHERE runId = :runId")
    suspend fun finishSession(runId: String)

    // 위치 점 추가
    @Insert
    suspend fun insertLocation(location: LocationEntity)

    // [복구용] 아직 안 끝난(isFinished=false) 최신 세션 가져오기
    @Query("SELECT * FROM run_sessions WHERE isFinished = 0 ORDER BY startTime DESC LIMIT 1")
    suspend fun getUnfinishedSession(): RunSessionEntity?

    // [복구용] 특정 세션의 모든 좌표 가져오기 (시간순 정렬)
    @Query("SELECT * FROM location_points WHERE runSessionId = :runId ORDER BY timestamp ASC")
    suspend fun getLocationsBySession(runId: String): List<LocationEntity>
}

@Database(entities = [RunSessionEntity::class, LocationEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun runningDao(): RunningDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "runners_hi.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

