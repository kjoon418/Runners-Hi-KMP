package good.space.runnershi.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import good.space.runnershi.model.domain.LocationModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidLocationTracker(
    private val context: Context
) : LocationTracker {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission") // 권한 체크는 UI/ViewModel 레벨에서 선행되어야 함
    override fun startTracking(): Flow<LocationModel> = callbackFlow {
        // 1. 위치 요청 설정 (배터리와 정확도 타협점 설정)
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, // 러닝 앱이므로 높은 정확도 필요
            3000L // 3초마다 업데이트
        ).apply {
            setMinUpdateIntervalMillis(1000L) // 최소 1초 간격
            setMinUpdateDistanceMeters(2f)    // 최소 2m 이동 시 갱신
        }.build()

        // 2. 콜백 정의
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    // 안드로이드 Location -> 공통 LocationModel 변환
                    val model = LocationModel(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestamp = location.time,
                        speed = location.speed // m/s 단위
                    )
                    // Flow로 방출
                    trySend(model) 
                }
            }
        }

        // 3. 업데이트 요청 시작
        client.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        ).addOnFailureListener { e ->
            close(e) // 실패 시 Flow 종료
        }

        // 4. Flow 수집이 끝날 때 호출
        awaitClose {
            client.removeLocationUpdates(locationCallback)
        }
    }

    override fun stopTracking() {
        // callbackFlow의 awaitClose에서 처리가 되므로 별도 구현 불필요할 수 있으나,
        // 명시적 리소스 해제가 필요한 경우 여기에 작성
    }
}

