package good.space.runnershi.model.domain

// UI나 비즈니스 로직에서 사용할 순수 위치 데이터
data class LocationModel(
    val latitude: Double,
    val longitude: Double,
    // val altitude: Double, // [삭제] 일반 러닝 앱에서는 불필요
    val timestamp: Long
)

