package good.space.runnershi.model.dto.running.percentile.response

data class RunPercentileResponse(
    val distanceBucket: String,      // "8-10" 같은 버킷
    val paceSecPerKm: Int,           // 내 페이스(초/킬로미터)
    val totalSamples: Long,          // 표본 수
    val topPercent: Double?,         // 상위 몇 % (작을수록 잘함)
)
