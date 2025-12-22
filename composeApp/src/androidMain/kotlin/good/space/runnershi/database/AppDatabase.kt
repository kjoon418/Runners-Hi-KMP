package good.space.runnershi.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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

    // 위치 점 벌크 삽입 (버퍼 기반 최적화)
    @Insert
    suspend fun insertLocations(locations: List<LocationEntity>)

    // [복구용] 아직 안 끝난(isFinished=false) 최신 세션 가져오기
    @Query("SELECT * FROM run_sessions WHERE isFinished = 0 ORDER BY startTime DESC LIMIT 1")
    suspend fun getUnfinishedSession(): RunSessionEntity?
    
    // [삭제용] 최신 세션 가져오기 (완료 여부 무관)
    @Query("SELECT * FROM run_sessions ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestSession(): RunSessionEntity?

    // [복구용] 특정 세션의 모든 좌표 가져오기 (시간순 정렬)
    @Query("SELECT * FROM location_points WHERE runSessionId = :runId ORDER BY timestamp ASC")
    suspend fun getLocationsBySession(runId: String): List<LocationEntity>

    // [폐기용] 세션 삭제 (CASCADE로 좌표도 자동 삭제됨)
    @Query("DELETE FROM run_sessions WHERE runId = :runId")
    suspend fun deleteSessionById(runId: String)
    
    // [로그아웃용] 모든 세션 삭제 (완료된 것 포함, CASCADE로 좌표도 자동 삭제됨)
    @Query("DELETE FROM run_sessions")
    suspend fun deleteAllSessions()
}

@Database(entities = [RunSessionEntity::class, LocationEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun runningDao(): RunningDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // [마이그레이션] version 1 -> 2: altitude 컬럼 제거
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // location_points 테이블에서 altitude 컬럼 제거
                // SQLite는 컬럼 삭제를 직접 지원하지 않으므로, 새 테이블을 만들고 데이터 복사
                db.execSQL("""
                    CREATE TABLE location_points_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        runSessionId TEXT NOT NULL,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        timestamp INTEGER NOT NULL,
                        segmentIndex INTEGER NOT NULL,
                        FOREIGN KEY(runSessionId) REFERENCES run_sessions(runId) ON DELETE CASCADE
                    )
                """.trimIndent())
                
                db.execSQL("""
                    INSERT INTO location_points_new (id, runSessionId, latitude, longitude, timestamp, segmentIndex)
                    SELECT id, runSessionId, latitude, longitude, timestamp, segmentIndex
                    FROM location_points
                """.trimIndent())
                
                db.execSQL("DROP TABLE location_points")
                db.execSQL("ALTER TABLE location_points_new RENAME TO location_points")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "runners_hi.db"
                )
                .addMigrations(MIGRATION_1_2)
                .build().also { INSTANCE = it }
            }
        }
    }
}

