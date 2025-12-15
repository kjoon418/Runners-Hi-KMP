package good.space.runnershi.util

/**
 * Long 타임스탬프(밀리초)를 ISO-8601 형식 문자열로 변환
 * 예: 1715756400000L -> "2024-05-15T06:30:00Z"
 * 
 * 참고: KMP에서는 kotlinx-datetime을 사용하는 것이 더 적합하지만,
 * 현재는 expect/actual 패턴으로 플랫폼별 구현을 제공합니다.
 * 추후 kotlinx-datetime으로 교체 가능합니다.
 */
expect object TimeConverter {
    fun toIso8601(timestampMillis: Long): String
}

