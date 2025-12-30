package good.space.runnershi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import good.space.runnershi.ui.theme.RunnersHiTheme
import good.space.runnershi.util.format
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PersonalBestIndicator(
    currentDistanceMeters: Double,
    bestDistanceMeters: Double,
    modifier: Modifier = Modifier
) {
    val progressPercent = calculateProgressPercent(currentDistanceMeters, bestDistanceMeters)
    val isAchieveBest = isAchieveBest(progressPercent)

    // 색상 팔레트
    val backgroundColor = RunnersHiTheme.custom.cardBackground.copy(alpha = 0.8f)
    val normalContentColor = RunnersHiTheme.colorScheme.background
    val goldColor = RunnersHiTheme.custom.personalBestAchieve

    // 상태에 따른 활성 색상
    val activeColor = if (isAchieveBest) goldColor else normalContentColor
    val trackBackgroundColor = Color.Gray.copy(alpha = 0.4f)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .widthIn(max = 100.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // 트로피, 진척도
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(24.dp)
            ) {
                // 배경 트랙
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.matchParentSize(),
                    color = trackBackgroundColor,
                    strokeWidth = 4.dp,
                    trackColor = Color.Transparent,
                )

                // 실제 진척도
                CircularProgressIndicator(
                    progress = { progressPercent },
                    modifier = Modifier.matchParentSize(),
                    color = activeColor,
                    strokeWidth = 2.dp,
                    trackColor = Color.Transparent,
                    strokeCap = StrokeCap.Round
                )

                // 트로피 아이콘
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Trophy",
                    tint = activeColor,
                    modifier = Modifier.size(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 텍스트 정보
            Column {
                Text(
                    text = "최고 기록",
                    color = activeColor,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp
                    ),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "%.2f km".format(bestDistanceMeters / 1000),
                    color = activeColor,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

private fun calculateProgressPercent(
    currentDistanceMeters: Double,
    bestDistanceMeters: Double
): Float {
    return (currentDistanceMeters / bestDistanceMeters).toFloat().coerceIn(0f, 1f)
}
private fun isAchieveBest(
    progressPercent: Float
): Boolean {
    return progressPercent >= 1f
}

@Preview
@Composable
private fun PersonalBestIndicatorPreview() {
    RunnersHiTheme {
        Column(
            modifier = Modifier
                .background(RunnersHiTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // 진행 중 (50%)
            PersonalBestIndicator(
                currentDistanceMeters = 2500.0,
                bestDistanceMeters = 5000.0
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 달성 완료
            PersonalBestIndicator(
                currentDistanceMeters = 5200.0, // 현재가 더 큼
                bestDistanceMeters = 5000.0
            )
        }
    }
}
