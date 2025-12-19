package good.space.runnershi.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import good.space.runnershi.model.domain.RunResult
import good.space.runnershi.ui.component.ServerSuccessBanner
import good.space.runnershi.util.TimeFormatter
import good.space.runnershi.util.format
import good.space.runnershi.viewmodel.UploadState

@Composable
fun RunResultScreen(
    result: RunResult,
    uploadState: UploadState,
    onClose: () -> Unit
) {
    val cameraPositionState = rememberCameraPositionState()
    
    // 저장 조건 체크 (ViewModel의 로직과 동일하게 유지)
    val isShortRun = remember(result) {
        result.totalDistanceMeters < 300.0 || result.duration.inWholeSeconds < 180
    }

    // 화면 진입 시 전체 경로가 보이도록 줌 아웃 (LatLngBounds)
    LaunchedEffect(Unit) {
        if (result.pathSegments.flatten().isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            result.pathSegments.flatten().forEach { 
                boundsBuilder.include(LatLng(it.latitude, it.longitude)) 
            }
            try {
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100) // 100px padding
                )
            } catch (_: Exception) {
                // 경로가 너무 작거나 없을 때 예외 처리
            }
        }
    }

    // 스크롤 가능한 컬럼 사용
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // ------------------------------------------------
        // 📢 인라인 배너 영역
        // ------------------------------------------------
        
        // 1. [성공] 서버 저장 완료 시
        if (uploadState == UploadState.SUCCESS) {
            ServerSuccessBanner()
        }
        
        // 2. [로딩] 업로드 중일 때
        if (uploadState == UploadState.UPLOADING) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
        
        // 3. [경고] 기록 미달 시
        if (isShortRun) {
            NotSavedWarningBanner()
        }

        // [상단] 지도 스냅샷 (조작 불가, 높이 고정)
        Box(modifier = Modifier.height(300.dp).fillMaxWidth()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    scrollGesturesEnabled = false,
                    zoomGesturesEnabled = false,
                    myLocationButtonEnabled = false
                )
            ) {
                result.pathSegments.forEach { segment ->
                    if (segment.isNotEmpty()) {
                        Polyline(
                            points = segment.map { LatLng(it.latitude, it.longitude) },
                            color = Color(0xFF6200EE),
                            width = 12f
                        )
                    }
                }
            }
        }

        // [하단] 요약 정보
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Great Run! 🎉", 
                style = MaterialTheme.typography.headlineMedium, 
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 메인 통계 (거리, 시간)
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResultItem("Distance", "%.2f km".format(result.totalDistanceMeters / 1000))
                ResultItem("Time", TimeFormatter.formatSecondsToTime(result.duration.inWholeSeconds))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 페이스 분석 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "페이스 분석",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 1. 이동 페이스 (Moving Pace)
                        StatItem(
                            label = "이동 페이스",
                            value = result.movingPace,
                            subLabel = "(휴식 제외)"
                        )
                        
                        // 2. 전체 페이스 (Elapsed Pace)
                        StatItem(
                            label = "전체 페이스",
                            value = result.elapsedPace,
                            subLabel = "(휴식 포함)"
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 시간 분석
                    Text(
                        text = "시간 분석",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "이동 시간",
                            value = TimeFormatter.formatSecondsToTime(result.duration.inWholeSeconds)
                        )
                        StatItem(
                            label = "총 소요 시간",
                            value = TimeFormatter.formatSecondsToTime(result.totalTime.inWholeSeconds)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            // ------------------------------------------------
            // 🔘 하단 버튼 영역
            // ------------------------------------------------
            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Close",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun NotSavedWarningBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), // 좌우, 상하 여백
        colors = CardDefaults.cardColors(
            // Material3의 에러 색상 테마 사용 (자동으로 다크모드 대응)
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp) // 둥근 모서리
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp) // 내부 여백
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top // 텍스트가 길어질 경우를 대비해 상단 정렬
        ) {
            // 1. 아이콘
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Warning",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 2. 텍스트 영역
            Column {
                Text(
                    text = "이 기록은 저장되지 않습니다",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "유효한 러닝 기록(거리 300m 이상, 시간 3분 이상)만 히스토리에 저장됩니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun ResultItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label, 
            color = Color.Gray, 
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            value, 
            fontWeight = FontWeight.Bold, 
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun StatItem(label: String, value: String, subLabel: String? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(0.45f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        if (subLabel != null) {
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

