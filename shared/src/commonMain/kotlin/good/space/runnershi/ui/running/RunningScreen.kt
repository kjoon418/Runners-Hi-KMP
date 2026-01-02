package good.space.runnershi.ui.running

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import good.space.runnershi.model.domain.location.LocationModel
import good.space.runnershi.model.dto.running.LongestDistance
import good.space.runnershi.state.PauseType
import good.space.runnershi.ui.components.CalorieIndicator
import good.space.runnershi.ui.components.Logo
import good.space.runnershi.ui.components.MapCameraFocus
import good.space.runnershi.ui.components.PersonalBestIndicator
import good.space.runnershi.ui.components.RunControlPanel
import good.space.runnershi.ui.components.RunningMap
import good.space.runnershi.ui.components.VehicleWarningDialog
import good.space.runnershi.ui.theme.RunnersHiTheme
import good.space.runnershi.util.format
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RunningScreen(
    state: RunningUiState,
    onFinishClick: () -> Unit,
    onPauseResumeClick: () -> Unit,
    onVehicleResumeClick: () -> Unit,
    onForcedFinishClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        RunningMap(
            focus = MapCameraFocus.FollowLocation(state.currentLocation),
            pathSegments = state.pathSegments,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 20.dp, end = 20.dp)
        ) {
            // 좌측 상단: 칼로리, 최고 기록
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .width(100.dp)
            ) {
                CalorieIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    calories = state.currentCalories
                )

                // 최고 기록이 있을 때만 표시
                val bestDistance = state.personalBest?.longestDistance
                if (bestDistance != null && bestDistance > 0) {
                    Spacer(modifier = Modifier.height(8.dp))

                    PersonalBestIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        currentDistanceMeters = state.totalDistanceMeters,
                        bestDistanceMeters = bestDistance,
                    )
                }
            }

            // 우측 상단: 로고
            Column(
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Logo(100.dp)
            }
        }

        // 하단 컨트롤 패널
        RunControlPanel(
            modifier = Modifier
                .align(Alignment.BottomCenter),
            distance = "%.2f km".format(state.totalDistanceMeters / 1000.0),
            pace = state.currentPace,
            time = formatSecondsToTime(state.durationSeconds),
            isRunning = state.isRunning,
            onFinishClick = onFinishClick,
            onPauseResumeClick = onPauseResumeClick
        )

        if (state.pauseType == PauseType.AUTO_PAUSE_VEHICLE) {
            VehicleWarningDialog(
                isForcedStop = state.vehicleWarningCount > 1,
                onResumeClick = onVehicleResumeClick,
                onFinishClick = onForcedFinishClick
            )
        }
    }
}

private fun formatSecondsToTime(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return "%02d:%02d".format(minutes, seconds)
}

@Preview
@Composable
private fun RunningScreenActivePreview() {
    val activeState = RunningUiState(
        currentLocation = dummyLocation,
        pathSegments = dummyPath,
        totalDistanceMeters = 3250.0,
        durationSeconds = 1234L,
        currentPace = "6'20''",
        currentCalories = 350,
        isRunning = true,
        personalBest = dummyPersonalBest
    )

    RunnersHiTheme {
        Box(modifier = Modifier.background(Color(0xFFEEEEEE))) {
            RunningScreen(
                state = activeState,
                onFinishClick = {},
                onPauseResumeClick = {},
                onVehicleResumeClick = {},
                onForcedFinishClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun RunningScreenPausedPreview() {
    val pausedState = RunningUiState(
        currentLocation = dummyLocation,
        pathSegments = emptyList(),
        totalDistanceMeters = 0.0,
        durationSeconds = 0L,
        currentPace = "-'--''",
        currentCalories = 0,
        isRunning = false,
        personalBest = dummyPersonalBest
    )

    RunnersHiTheme {
        Box(modifier = Modifier.background(Color(0xFFEEEEEE))) {
            RunningScreen(
                state = pausedState,
                onFinishClick = {},
                onPauseResumeClick = {},
                onVehicleResumeClick = {},
                onForcedFinishClick = {},
            )
        }
    }
}

// 프리뷰용 더미 데이터
private val dummyLocation =
    LocationModel(latitude = 37.5665, longitude = 126.9780, timestamp = 0L)
private val dummyPath = listOf(
    listOf(
        LocationModel(37.5665, 126.9780, 0, 0F),
        LocationModel(37.5675, 126.9785, 0, 0F),
        LocationModel(37.5685, 126.9795, 0, 0F)
    )
)
private val dummyPersonalBest = LongestDistance(longestDistance = 5000.0) // 5km PB
