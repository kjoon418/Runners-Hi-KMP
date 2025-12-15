package good.space.runnershi.util

import platform.Foundation.NSDate
import platform.Foundation.NSISO8601DateFormatter

actual object TimeConverter {
    actual fun toIso8601(timestampMillis: Long): String {
        val date = NSDate.dateWithTimeIntervalSince1970(timestampMillis / 1000.0)
        val formatter = NSISO8601DateFormatter()
        return formatter.stringFromDate(date) ?: ""
    }
}

