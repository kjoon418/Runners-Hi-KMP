// androidMain/good/space/runnershi/ui/components/RunningMap.android.kt
package good.space.runnershi.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import good.space.runnershi.model.domain.location.LocationModel
import good.space.runnershi.util.MapsApiKeyChecker
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
actual fun RunningMap(
    focus: MapCameraFocus,
    pathSegments: List<List<LocationModel>>,
    modifier: Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val isInspection = LocalInspectionMode.current

    val isApiKeySet = remember {
        isInspection || MapsApiKeyChecker.isApiKeySet(context)
    }

    val hasLocationPermission = remember(context) { checkLocationPermission(context) }
    val cameraPositionState = rememberCameraPositionState()

    val polylines by remember(pathSegments) {
        derivedStateOf {
            pathSegments.map { segment ->
                segment.map { LatLng(it.latitude, it.longitude) }
            }
        }
    }

    MapCameraHandler(
        focus = focus,
        cameraPositionState = cameraPositionState,
        paddingPx = with(density) { 50.dp.roundToPx() }
    )

    Box(modifier = modifier) {
        if (isApiKeySet) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission && focus is MapCameraFocus.FollowLocation
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false,
                    compassEnabled = true
                )
            ) {
                polylines.forEach { points ->
                    if (points.isNotEmpty()) {
                        Polyline(
                            points = points,
                            color = Color(0xFF6200EE), // 추후 테마 컬러 적용
                            width = 15f,
                            jointType = JointType.ROUND,
                            startCap = RoundCap(),
                            endCap = RoundCap()
                        )
                    }
                }

                if (focus is MapCameraFocus.FitPath && polylines.isNotEmpty()) {
                    val allPoints = polylines.flatten()
                    if (allPoints.isNotEmpty()) {
                        Marker(
                            state = MarkerState(position = allPoints.first()),
                            title = "Start",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                        )
                        Marker(
                            state = MarkerState(position = allPoints.last()),
                            title = "Finish",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                    }
                }
            }
        } else {
            NoApiKeyPlaceholder()
        }
    }
}

@Composable
private fun MapCameraHandler(
    focus: MapCameraFocus,
    cameraPositionState: CameraPositionState,
    paddingPx: Int
) {
    // focus가 바뀌거나 내부의 location 값이 바뀔 때 재실행
    LaunchedEffect(focus) {
        when (focus) {
            is MapCameraFocus.FollowLocation -> {
                focus.location?.let { loc ->
                    val target = LatLng(loc.latitude, loc.longitude)
                    cameraPositionState.animate(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.fromLatLngZoom(target, focus.zoom)
                        ),
                        1000
                    )
                }
            }
            is MapCameraFocus.FitPath -> {
                val boundsBuilder = LatLngBounds.Builder()
                var hasPoints = false

                focus.path.flatten().forEach {
                    boundsBuilder.include(LatLng(it.latitude, it.longitude))
                    hasPoints = true
                }

                if (hasPoints) {
                    try {
                        val bounds = boundsBuilder.build()
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngBounds(bounds, paddingPx)
                        )
                    } catch (_: Exception) {
                        // Bounds 계산 실패 시 예외 처리
                    }
                }
            }
        }
    }
}

private fun checkLocationPermission(context: Context): Boolean {
    val hasFineLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val hasCoarseLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return hasFineLocation || hasCoarseLocation
}

@Composable
private fun NoApiKeyPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️ Google Maps API Key 필요",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6200EE),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, name = "Running Mode")
@Composable
private fun RunningMapFollowPreview() {
    val startLat = 37.5665
    val startLng = 126.9780
    val currentLocation = LocationModel(startLat + 0.002, startLng + 0.001, 0, 0.0F)

    MaterialTheme {
        RunningMap(
            focus = MapCameraFocus.FollowLocation(currentLocation),
            pathSegments = emptyList(),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true, name = "Result Mode")
@Composable
private fun RunningMapFitPreview() {
    val startLat = 37.5665
    val startLng = 126.9780

    val path = listOf(
        listOf(
            LocationModel(startLat, startLng, 0, 0.0F),
            LocationModel(startLat + 0.01, startLng + 0.01, 0, 0.0F)
        )
    )

    MaterialTheme {
        RunningMap(
            focus = MapCameraFocus.FitPath(path),
            pathSegments = path,
            modifier = Modifier.fillMaxSize()
        )
    }
}
