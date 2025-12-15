package good.space.runnershi.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// 1. 러닝 세션 (헤더 정보)
@Entity(tableName = "run_sessions")
data class RunSessionEntity(
    @PrimaryKey val runId: String, // UUID
    val startTime: Long,
    val totalDistance: Double,
    val durationSeconds: Long,
    val isFinished: Boolean = false // false면 비정상 종료된 것으로 간주
)

// 2. 위치 포인트 (바디 정보)
@Entity(
    tableName = "location_points",
    foreignKeys = [
        ForeignKey(
            entity = RunSessionEntity::class,
            parentColumns = ["runId"],
            childColumns = ["runSessionId"],
            onDelete = ForeignKey.CASCADE // 세션 지우면 좌표도 다 지움
        )
    ]
)
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val runSessionId: String,
    val latitude: Double,
    val longitude: Double,
    // val altitude: Double, // [삭제] 일반 러닝 앱에서는 불필요
    val timestamp: Long,
    val segmentIndex: Int // [중요] 몇 번째 선분인지 (0, 1, 2...)
)

