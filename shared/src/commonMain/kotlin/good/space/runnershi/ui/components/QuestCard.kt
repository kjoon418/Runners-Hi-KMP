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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
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
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun QuestCard(
    title: String,
    exp: Long,
    isCleared: Boolean = false,
    description: String? = null,
    modifier: Modifier = Modifier
) {
    val customColors = RunnersHiTheme.custom
    val colorScheme = RunnersHiTheme.colorScheme

    // 배경 그라데이션 설정
    val backgroundBrush = if (isCleared) {
        Brush.horizontalGradient(
            colors = listOf(
                customColors.clearedQuestDark,
                customColors.clearedQuestLight
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                customColors.questDark,
                customColors.questLight
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = colorScheme.onBackground.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(brush = backgroundBrush)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 제목
                Text(
                    text = title,
                    style = RunnersHiTheme.typography.titleMedium,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )

                // 경험치 or 체크표시
                if (isCleared) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Cleared",
                        tint = customColors.resumeDark,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    val expText = if (description != null) "+ $exp EXP" else "$exp EXP"
                    Text(
                        text = expText,
                        style = RunnersHiTheme.typography.labelLarge,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = customColors.resumeLight
                    )
                }
            }

            // 설명
            if (description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = RunnersHiTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview
@Composable
fun QuestListPreview() {
    RunnersHiTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // 일반 퀘스트
            QuestCard(title = "3km 달리기", exp = 100, isCleared = false)

            // 클리어된 퀘스트
            QuestCard(title = "15분 달리기", exp = 100, isCleared = true)

            // 업적
            QuestCard(
                title = "알바트로스",
                exp = 800,
                isCleared = false,
                description = "총 러닝 거리 3,000km 돌파!"
            )
        }
    }
}
