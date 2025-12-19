package good.space.runnershi.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
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
import good.space.runnershi.service.AndroidServiceController
import good.space.runnershi.state.PauseType
import good.space.runnershi.ui.component.PersonalBestIndicator
import good.space.runnershi.util.MapsApiKeyChecker
import good.space.runnershi.util.TimeFormatter
import good.space.runnershi.viewmodel.MainViewModel
import good.space.runnershi.viewmodel.RunningViewModel
import kotlinx.coroutines.launch

@Composable
fun RunningScreen(
    viewModel: RunningViewModel,
    mainViewModel: MainViewModel,
    serviceController: AndroidServiceController // 서비스 제어용 (다이얼로그에서 RESUME 호출 시 필요)
) {
    val context = LocalContext.current
    
    // 1. 상태 구독 (StateFlow -> Compose State)
    val currentLocation by viewModel.currentLocation.collectAsState()
    val pathSegments by viewModel.pathSegments.collectAsState()
    val totalDistance by viewModel.totalDistanceMeters.collectAsState()
    val durationSeconds by viewModel.durationSeconds.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val personalBest by viewModel.personalBest.collectAsState() // 최대 기록
    val pauseType by viewModel.pauseType.collectAsState()
    
    // [New] 차량 경고 횟수 구독
    val vehicleWarningCount by viewModel.vehicleWarningCount.collectAsState()
    
    // [New] 강제 종료 다이얼로그 표시 여부 상태
    var showForcedFinishDialog by remember { mutableStateOf(false) }

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
    
    // [Logic Change] 2회 누적 시 -> 즉시 종료가 아니라 '다이얼로그'를 띄움
    LaunchedEffect(vehicleWarningCount) {
        if (vehicleWarningCount >= 2) {
            showForcedFinishDialog = true
        }
    }

    // API 키 확인
    val isApiKeySet = remember { MapsApiKeyChecker.isApiKeySet(context) }
    
    // 로그아웃 처리
    val scope = rememberCoroutineScope()
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    
    // 설정값 구독
    val isAutoPauseEnabled by mainViewModel.isAutoPauseEnabled.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }

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
            // API 키 없음 UI
            NoApiKeyPlaceholder()
        }
        
        // --- [New] 오토 퍼즈 상태 표시 배지 (상단 중앙) ---
        AnimatedVisibility(
            visible = pauseType == PauseType.AUTO_PAUSE_REST,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⏸️  휴식 감지됨 (자동 일시정지)", color = Color.White)
                }
            }
        }

        // --- 상단 우측 버튼 그룹 (설정 + 로그아웃) ---
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp) // 버튼 사이 간격
        ) {
            // 1. 설정 버튼 (톱니바퀴)
            SettingsButton(onClick = { showSettingsDialog = true })
            
            // 2. 로그아웃 버튼
            LogoutButton(
                onLogoutClick = {
                    if (isRunning || durationSeconds > 0) {
                        showLogoutConfirmDialog = true
                    } else {
                        scope.launch { mainViewModel.logout() }
                    }
                }
            )
        }
        
        if (showLogoutConfirmDialog) {
            LogoutConfirmDialog(
                onConfirm = {
                    scope.launch {
                        mainViewModel.logout()
                        showLogoutConfirmDialog = false
                    }
                },
                onDismiss = { showLogoutConfirmDialog = false }
            )
        }

        // --- [D] 최대 기록 인디케이터 (좌측 상단) ---
        PersonalBestIndicator(
            currentDistanceMeters = totalDistance,
            pbDistanceMeters = personalBest?.distanceMeters
        )

        // --- [B] 정보 및 컨트롤 패널 (HUD) ---
        RunControlPanel(
            modifier = Modifier.align(Alignment.BottomCenter),
            isRunning = isRunning,
            durationSeconds = durationSeconds,
            totalDistance = totalDistance,
            onStartResume = { viewModel.startRun() },
            onPause = { viewModel.pauseRun() },
            onFinish = { viewModel.finishRun() }
        )
        
        // [New] 2회 누적 강제 종료 알림 다이얼로그 (최우선)
        if (showForcedFinishDialog) {
            ForcedFinishDialog(
                onConfirm = {
                    // 사용자가 확인 버튼을 누르면 비로소 종료 처리 및 화면 이동
                    viewModel.finishRun()
                }
            )
        }
        
        // [Mod] 1회차 경고 다이얼로그 (조건: 카운트가 2 미만일 때만)
        if (pauseType == PauseType.AUTO_PAUSE_VEHICLE && vehicleWarningCount < 2) {
            VehicleWarningDialog(
                onResume = { 
                    // 경고를 무시하고 다시 달리기
                    viewModel.startRun() 
                },
                onFinishRun = { 
                    viewModel.finishRun() 
                }
            )
        }
        
        // 설정 다이얼로그 표시
        if (showSettingsDialog) {
            SettingsDialog(
                isAutoPauseEnabled = isAutoPauseEnabled,
                onToggleAutoPause = { mainViewModel.toggleAutoPause() },
                onDismiss = { showSettingsDialog = false }
            )
        }
    }
}

// ------------------------------------------------------------------------
// 분리된 컴포넌트들
// ------------------------------------------------------------------------

@Composable
fun VehicleWarningDialog(onResume: () -> Unit, onFinishRun: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* 강제 종료 방지 */ },
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFA000)) },
        title = { Text(text = "이동 속도가 너무 빠릅니다") },
        text = { 
            Text(
                text = "차량 탑승이 감지되어 기록을 일시정지했습니다.\n이동 데이터는 저장되지 않았습니다.\n\n계속 뛰시겠습니까?",
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text("다시 달리기")
            }
        },
        dismissButton = {
            TextButton(onClick = onFinishRun) {
                Text("러닝 종료", color = Color.Red)
            }
        }
    )
}

/**
 * [New] 강제 종료 안내 다이얼로그 컴포넌트
 * 차량 탑승이 2회 이상 감지되어 러닝이 강제 종료되었을 때 표시됩니다.
 */
@Composable
fun ForcedFinishDialog(onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = { 
            // 뒤로가기 키를 눌러도 종료 처리 (다이얼로그만 닫히고 맵에 남는 것 방지)
            onConfirm() 
        },
        icon = { 
            Icon(
                imageVector = Icons.Default.Warning, 
                contentDescription = null, 
                tint = Color.Red
            ) 
        },
        title = { 
            Text(
                text = "러닝이 종료되었습니다",
                fontWeight = FontWeight.Bold
            ) 
        },
        text = { 
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "차량 탑승이 반복 감지되었습니다.",
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "데이터 정확도를 위해\n현재까지의 기록으로 러닝을 마칩니다.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("결과 확인하기")
            }
        },
        // dismissButton은 없음 (선택권 없음, 무조건 종료)
    )
}

@Composable
fun RunControlPanel(
    modifier: Modifier = Modifier,
    isRunning: Boolean,
    durationSeconds: Long,
    totalDistance: Double,
    onStartResume: () -> Unit,
    onPause: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = modifier
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
                    onClick = onStartResume,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                ) {
                    Text(text = if (durationSeconds > 0) "RESUME" else "START")
                }
                // FINISH (기록이 있을 때만)
                if (durationSeconds > 0) {
                    Button(
                        onClick = onFinish,
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text(text = "FINISH")
                    }
                }
            } else {
                // 러닝 중: PAUSE + FINISH
                Button(
                    onClick = onPause,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))
                ) {
                    Text(text = "PAUSE")
                }
                Button(
                    onClick = onFinish,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(text = "FINISH")
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
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SettingsButton(onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 4.dp
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "설정",
                tint = Color(0xFF6200EE)
            )
        }
    }
}

@Composable
fun LogoutButton(modifier: Modifier = Modifier, onLogoutClick: () -> Unit) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 4.dp
    ) {
        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier.padding(0.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6200EE))
        ) {
            Text(text = "로그아웃", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun LogoutConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("로그아웃 확인") },
        text = { Text("현재 러닝 기록은 제거됩니다. 정말 로그아웃하시겠습니까?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("로그아웃", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
fun SettingsDialog(
    isAutoPauseEnabled: Boolean,
    onToggleAutoPause: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("러닝 설정") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "자동 일시정지",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "멈추면 기록을 자동으로 중단합니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = isAutoPauseEnabled,
                        onCheckedChange = { onToggleAutoPause() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF6200EE),
                            checkedTrackColor = Color(0xFF6200EE).copy(alpha = 0.5f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기")
            }
        }
    )
}

@Composable
fun NoApiKeyPlaceholder() {
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

