package good.space.runnershi.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import good.space.runnershi.ui.theme.RunnersHiTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import runnershi.shared.generated.resources.Res
import runnershi.shared.generated.resources.trophy

@Composable
fun TrophyCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    val customColors = RunnersHiTheme.custom
    val colorScheme = RunnersHiTheme.colorScheme

    val backgroundBrush = Brush.horizontalGradient(
        colors = listOf(
            customColors.trophyLight,
            customColors.trophyDark
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = colorScheme.onBackground.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(brush = backgroundBrush)
            .padding(vertical = 20.dp, horizontal = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 아이콘 슬롯 (이미지 아이콘이나 벡터 아이콘을 유연하게 받음)
            Box(
                modifier = Modifier.size(56.dp), // 아이콘 영역 크기 확보
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.trophy),
                    contentDescription = "Trophy"
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                // 제목: 상위 43% 페이스
                Text(
                    text = title,
                    style = RunnersHiTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground // 검은색 계열 (가독성 확보)
                    )
                )
                
                // 설명: 평균보다 빨라요!
                Text(
                    text = description,
                    style = RunnersHiTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurfaceVariant // 살짝 연한 회색 (Gray700)
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun AchievementCardPreview() {
    RunnersHiTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            TrophyCard(
                title = "상위 43% 페이스",
                description = "평균보다 빨라요!"
            )
        }
    }
}
