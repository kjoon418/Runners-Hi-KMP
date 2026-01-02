package good.space.runnershi.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import good.space.runnershi.ui.theme.RunnersHiTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import runnershi.shared.generated.resources.Res
import runnershi.shared.generated.resources.star_character

data class AchievementData(
    val title: String,
    val description: String,
    val exp: Long
)

@Composable
fun AchievementDialog(
    achievements: List<AchievementData>,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 등장 애니메이션을 위한 상태
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val trophyGradient = Brush.verticalGradient(
        0.0f to RunnersHiTheme.custom.trophyLight,
        0.65f to RunnersHiTheme.custom.trophyLight,
        1.0f to RunnersHiTheme.custom.trophyDark
    )

    // 전체 화면 오버레이
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismissRequest() },
        contentAlignment = Alignment.Center
    ) {
        // 중앙 다이얼로그 카드
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(tween(300)) + fadeIn(tween(300))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = RunnersHiTheme.custom.trophyLight,
                        ambientColor = Color.White
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .background(brush = trophyGradient)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismissRequest() }
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // 캐릭터
                Image(
                    painter = painterResource(Res.drawable.star_character),
                    contentDescription = "Achievement Star",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 타이틀
                Text(
                    text = "업적 달성!",
                    style = RunnersHiTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = RunnersHiTheme.colorScheme.onBackground
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 업적 리스트
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    achievements.forEach { item ->
                        QuestCard(
                            title = item.title,
                            description = item.description,
                            exp = item.exp,
                            isCleared = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AchievementDialogPreview() {
    val dummyAchievements = listOf(
        AchievementData(
            title = "알바트로스",
            description = "총 러닝 거리 3,000km 돌파!",
            exp = 800
        ),
        AchievementData(
            title = "천천히 꾸준히",
            description = "7일 연속 러닝!",
            exp = 400
        )
    )

    RunnersHiTheme {
        Box(
            modifier = Modifier
                .background(RunnersHiTheme.colorScheme.background)
        ) {
            AchievementDialog(
                achievements = dummyAchievements,
                onDismissRequest = {}
            )
        }
    }
}
