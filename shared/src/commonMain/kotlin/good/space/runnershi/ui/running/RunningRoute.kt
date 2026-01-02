package good.space.runnershi.ui.running

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import good.space.runnershi.model.dto.user.UpdatedUserResponse
import good.space.runnershi.permission.HandleLocationPermission
import good.space.runnershi.permission.LocationPermissionManager
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RunningRoute(
    navigateToResult: (UpdatedUserResponse?, RunningResultToShow) -> Unit,
    viewModel: RunningViewModel = koinViewModel()
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val permissionManager: LocationPermissionManager = koinInject()
    
    // 권한 상태 구독
    val hasPermission by permissionManager.hasPermission.collectAsState()
    
    // 플랫폼별 권한 요청 처리
    HandleLocationPermission(permissionManager = permissionManager)

    // ViewModel의 개별 StateFlow 구독
    val currentLocation by viewModel.currentLocation.collectAsState()
    val pathSegments by viewModel.pathSegments.collectAsState()
    val totalDistance by viewModel.totalDistanceMeters.collectAsState()
    val durationSeconds by viewModel.durationSeconds.collectAsState()
    val currentPace by viewModel.currentPace.collectAsState()
    val currentCalories by viewModel.currentCalories.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val personalBest by viewModel.personalBest.collectAsState()
    val pauseType by viewModel.pauseType.collectAsState()
    val vehicleWarningCount by viewModel.vehicleWarningCount.collectAsState()

    // 진입 시 키보드/포커스 정리 및 초기화
    LaunchedEffect(Unit) {
        // 1. 즉시 키보드와 포커스를 정리
        keyboardController?.hide()
        focusManager.clearFocus()

        // 2. 키보드 애니메이션이 완료되고 이전 화면의 레이아웃 재측정이 완료될 때까지 대기
        delay(300)

        // 3. 초기화 로직 실행
        if (!isRunning && durationSeconds == 0L) {
            viewModel.resetState()
        }
    }

    // UI State 객체 생성 (화면 렌더링용)
    val uiState = RunningUiState(
        currentLocation = currentLocation,
        pathSegments = pathSegments,
        totalDistanceMeters = totalDistance,
        durationSeconds = durationSeconds,
        currentPace = currentPace,
        currentCalories = currentCalories,
        isRunning = isRunning,
        personalBest = personalBest,
        pauseType = pauseType,
        vehicleWarningCount = vehicleWarningCount
    )

    // Side Effect 처리
    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is RunningUiEvent.NavigateToResult -> {
                    navigateToResult(event.userInfo, event.runResult)
                }
                is RunningUiEvent.RunNotUploadable -> {
                    navigateToResult(null, event.runResult)
                }
            }
        }
    }

    // UI 화면 호출
    RunningScreen(
        state = uiState,
        onFinishClick = viewModel::finishRun,
        onPauseResumeClick = {
            if (isRunning) viewModel.pauseRun() else viewModel.startRun()
        },
        onVehicleResumeClick = viewModel::startRun,
        onForcedFinishClick = viewModel::finishRun,
    )
}
