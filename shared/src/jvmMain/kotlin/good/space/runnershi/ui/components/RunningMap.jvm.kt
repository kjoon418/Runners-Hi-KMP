package good.space.runnershi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import good.space.runnershi.model.domain.location.LocationModel

@Composable
actual fun RunningMap(
    focus: MapCameraFocus,
    pathSegments: List<List<LocationModel>>,
    modifier: Modifier
) {
    // TODO: 러닝 맵 JVM 구현체 구현
}
