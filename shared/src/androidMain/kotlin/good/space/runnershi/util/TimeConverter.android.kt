package good.space.runnershi.util

import java.time.Instant

actual object TimeConverter {
    actual fun toIso8601(timestampMillis: Long): String {
        val instant = Instant.ofEpochMilli(timestampMillis)
        return instant.toString() // ISO-8601 형식으로 자동 변환
    }
}

