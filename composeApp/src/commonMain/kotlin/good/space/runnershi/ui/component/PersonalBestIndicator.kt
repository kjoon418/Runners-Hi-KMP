package good.space.runnershi.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import good.space.runnershi.util.format

@Composable
fun PersonalBestIndicator(
    currentDistanceMeters: Double,
    pbDistanceMeters: Double? // PB가 없을 수도 있음 (null)
) {
    // PB가 없으면(첫 러닝이면) 표시하지 않음
    if (pbDistanceMeters == null || pbDistanceMeters == 0.0) return

    // 진행률 계산 (0.0 ~ 1.0)
    val progress = (currentDistanceMeters / pbDistanceMeters).coerceIn(0.0, 1.0).toFloat()
    val isBroken = currentDistanceMeters >= pbDistanceMeters

    // 애니메이션 상태
    val animatedProgress by animateFloatAsState(targetValue = progress)
    val iconColor by animateColorAsState(targetValue = if (isBroken) Color(0xFFFFD700) else Color.White) // Gold or White
    val borderColor by animateColorAsState(targetValue = if (isBroken) Color(0xFFFFD700) else Color.Transparent)

    Row(
        modifier = Modifier
            .padding(top = 48.dp, start = 16.dp) // 상태바 공간 확보 및 여백
            .clip(RoundedCornerShape(50)) // 캡슐 모양
            .background(Color.Black.copy(alpha = 0.6f)) // 반투명 검정 배경
            .border(
                width = if (isBroken) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // [1] 프로그레스 아이콘
        Box(contentAlignment = Alignment.Center) {
            // 배경 원
            CircularProgressIndicator(
                progress = 1f,
                modifier = Modifier.size(28.dp),
                color = Color.Gray.copy(alpha = 0.3f),
                strokeWidth = 3.dp,
            )

            // 진행률 원
            CircularProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier.size(28.dp),
                color = if (isBroken) Color(0xFFFFD700) else Color.White, // Gold or White
                strokeWidth = 3.dp,
            )

            // 트로피 아이콘
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "PB Trophy",
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // [2] 텍스트 정보
        Column {
            Text(
                text = "My Best",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Text(
                text = "%.1f km".format(pbDistanceMeters / 1000.0),
                style = MaterialTheme.typography.labelLarge,
                color = if (isBroken) Color(0xFFFFD700) else Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

