package good.space.runnershi.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import good.space.runnershi.ui.component.PersonalBestIndicator
import good.space.runnershi.util.MapsApiKeyChecker
import good.space.runnershi.util.TimeFormatter
import good.space.runnershi.viewmodel.MainViewModel
import good.space.runnershi.viewmodel.RunningViewModel
import kotlinx.coroutines.launch

@Composable
fun RunningScreen(
    viewModel: RunningViewModel,
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current
    
    // 1. 상태 구독 (StateFlow -> Compose State)
    val currentLocation by viewModel.currentLocation.collectAsState()
    val pathSegments by viewModel.pathSegments.collectAsState()
    val totalDistance by viewModel.totalDistanceMeters.collectAsState()
    val durationSeconds by viewModel.durationSeconds.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val personalBest by viewModel.personalBest.collectAsState() // 최대 기록

    // 2. 구글 맵 카메라 상태
    val cameraPositionState = rememberCameraPositionState()

    // 3. 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions.values.all { it }
        if (isGranted) {
            // 권한 승인 시 로직 (필요하다면 초기 위치 로드 등)
        }
    }

    // 화면 진입 시 권한 요청
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
    }

    // 4. 위치가 바뀔 때마다 카메라 이동 (Follow User)
    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc ->
            val latLng = LatLng(loc.latitude, loc.longitude)
            // 줌 레벨 17f 정도로 부드럽게 이동
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(latLng, 17f)
                ),
                1000 // 1초 동안 애니메이션
            )
        }
    }

    // API 키 확인
    val isApiKeySet = remember { MapsApiKeyChecker.isApiKeySet(context) }
    
    // 로그아웃 처리
    val scope = rememberCoroutineScope()
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- [A] 지도 레이어 ---
        if (isApiKeySet) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true), // 파란 점 표시
                uiSettings = MapUiSettings(myLocationButtonEnabled = true)
            ) {
                // 경로 그리기 (Multi-Segment Polyline)
                pathSegments.forEach { segment ->
                    if (segment.isNotEmpty()) {
                        Polyline(
                            points = segment.map { LatLng(it.latitude, it.longitude) },
                            color = Color(0xFF6200EE), // 보라색 라인
                            width = 15f
                        )
                    }
                }
            }
        } else {
            // API 키가 없을 때 대체 UI 표시
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
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "지도를 표시하려면 Google Maps API 키가 필요합니다.\n\n" +
                            "AndroidManifest.xml 파일에서\n" +
                            "\"YOUR_API_KEY_HERE\"를 실제 API 키로 교체해주세요.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "API 키 발급:\nconsole.cloud.google.com/google/maps-apis",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6200EE),
                    textAlign = TextAlign.Center
                )
            }
        }

        // --- [C] 로그아웃 버튼 (상단 우측) - 지도 위에 표시 ---
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 4.dp
        ) {
            OutlinedButton(
                onClick = {
                    // 러닝 중이거나 기록이 있으면 확인 다이얼로그 표시
                    if (isRunning || durationSeconds > 0) {
                        showLogoutConfirmDialog = true
                    } else {
                        // 러닝 중이 아니면 바로 로그아웃
                        scope.launch {
                            mainViewModel.logout()
                        }
                    }
                },
                modifier = Modifier.padding(0.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF6200EE)
                )
            ) {
                Text(
                    text = "로그아웃",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        
        // 로그아웃 확인 다이얼로그
        if (showLogoutConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmDialog = false },
                title = { Text("로그아웃 확인") },
                text = { Text("현재 러닝 기록은 제거됩니다. 정말 로그아웃하시겠습니까?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                mainViewModel.logout()
                                showLogoutConfirmDialog = false
                            }
                        }
                    ) {
                        Text("로그아웃", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLogoutConfirmDialog = false }
                    ) {
                        Text("취소")
                    }
                }
            )
        }

        // --- [D] 최대 기록 인디케이터 (좌측 상단) - 지도 위에 표시 ---
        PersonalBestIndicator(
            currentDistanceMeters = totalDistance,
            pbDistanceMeters = personalBest?.distanceMeters
        )

        // --- [B] 정보 및 컨트롤 패널 (HUD) ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 타이머 & 거리 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RunStatItem(
                    label = "TIME",
                    value = TimeFormatter.formatSecondsToTime(durationSeconds)
                )
                RunStatItem(
                    label = "DISTANCE",
                    value = String.format("%.2f km", totalDistance / 1000.0)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 컨트롤 버튼
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!isRunning) {
                    // START / RESUME
                    Button(
                        onClick = { viewModel.startRun() },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                    ) {
                        Text(text = if (durationSeconds > 0) "RESUME" else "START")
                    }
                    // FINISH (기록이 있을 때만)
                    if (durationSeconds > 0) {
                        Button(
                            onClick = { viewModel.finishRun() },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text(text = "FINISH")
                        }
                    }
                } else {
                    // 러닝 중: PAUSE + FINISH 모두 표시
                    Button(
                        onClick = { viewModel.pauseRun() },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))
                    ) {
                        Text(text = "PAUSE")
                    }
                    Button(
                        onClick = { viewModel.finishRun() },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text(text = "FINISH")
                    }
                }
            }
        }
    }
}

@Composable
fun RunStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium, // 큰 글씨
            fontWeight = FontWeight.Bold
        )
    }
}

