package good.space.runnershi.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import good.space.runnershi.model.domain.location.LocationModel

sealed interface MapCameraFocus {
    // 실시간 러닝용
    data class FollowLocation(
        val location: LocationModel?,
        val zoom: Float = 17f
    ) : MapCameraFocus

    // 완료 화면용
    data class FitPath(
        val path: List<List<LocationModel>>,
        val padding: Int = 50
    ) : MapCameraFocus
}

@Composable
expect fun RunningMap(
    focus: MapCameraFocus,
    pathSegments: List<List<LocationModel>>,
    modifier: Modifier = Modifier
)
