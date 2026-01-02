package good.space.runnershi.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import good.space.runnershi.ui.character.CharacterAppearance
import good.space.runnershi.ui.character.LayeredCharacter
import good.space.runnershi.ui.character.defaultCharacterAppearance
import good.space.runnershi.ui.theme.RunnersHiTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ProfileCard(
    appearance: CharacterAppearance,
    level: Long,
    currentExp: Long,
    maxExp: Long,
    gainedExp: Long,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = RunnersHiTheme.colorScheme.onBackground.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(RunnersHiTheme.custom.questLight)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CharacterSection(appearance)

                Spacer(modifier = Modifier.width(16.dp))

                LevelSection(
                    level = level,
                    currentExp = currentExp,
                    maxExp = maxExp
                )

                GainExpSection(gainedExp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            ExpProgressBar(maxExp, currentExp)
        }
    }
}

@Composable
private fun CharacterSection(
    appearance: CharacterAppearance
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(RunnersHiTheme.custom.expProgressLight),
        contentAlignment = Alignment.TopCenter
    ) {
        LayeredCharacter(
            appearance = appearance,
            modifier = Modifier
                .requiredWidth(160.dp)
                .aspectRatio(1f)
                .offset(y = 25.dp),
            isPlaying = false
        )
    }
}

@Composable
private fun RowScope.LevelSection(
    level: Long,
    currentExp: Long,
    maxExp: Long
) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = "LV. $level",
            style = RunnersHiTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = RunnersHiTheme.colorScheme.onBackground
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${currentExp.formatWithComma()} / ${maxExp.formatWithComma()} EXP",
            style = RunnersHiTheme.typography.bodySmall.copy(
                color = RunnersHiTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun GainExpSection(
    gainedExp: Long
) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = "+ ${gainedExp.formatWithComma()} EXP",
            style = RunnersHiTheme.typography.titleMedium.copy(
                color = RunnersHiTheme.custom.resumeLight,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "이번 러닝 획득",
            style = RunnersHiTheme.typography.bodySmall.copy(
                color = RunnersHiTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun ExpProgressBar(
    maxExp: Long,
    currentExp: Long
) {
    val targetProgress = if (maxExp > 0) {
        (currentExp.toFloat() / maxExp.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "ExpProgress"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .clip(RoundedCornerShape(50))
            .background(RunnersHiTheme.custom.expProgressLight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = animatedProgress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(RunnersHiTheme.custom.expProgressDark)
        )
    }
}

fun Long.formatWithComma(): String {
    return this.toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
}

@Preview
@Composable
private fun ProfileCardPreview() {
    RunnersHiTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ProfileCard(
                appearance = defaultCharacterAppearance,
                level = 13,
                currentExp = 15000,
                maxExp = 30000,
                gainedExp = 1350
            )
        }
    }
}
