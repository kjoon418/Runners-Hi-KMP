package good.space.runnershi.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import good.space.runnershi.model.dto.running.PersonalBestResponse
import good.space.runnershi.util.TimeFormatter
import good.space.runnershi.util.format

@Composable
fun PersonalBestCard(
    personalBest: PersonalBestResponse?,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        // 그라데이션 배경 (Gold 느낌)
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFC107))
                    )
                )
                .padding(20.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (personalBest == null) {
                Text(
                    "아직 기록이 없습니다. 첫 러닝을 시작해보세요!",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 트로피 아이콘
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Trophy",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "PERSONAL BEST 🏆",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // 거리 (예: 12.5 km)
                        Text(
                            text = "%.2f km".format(personalBest.distanceMeters / 1000.0),
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))

                        // 시간 및 날짜
                        Row {
                            Text(
                                text = "⏱ ${TimeFormatter.formatSecondsToTime(personalBest.durationSeconds)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "📅 ${personalBest.startedAt.take(10)}", // 날짜만 자르기 (YYYY-MM-DD)
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

