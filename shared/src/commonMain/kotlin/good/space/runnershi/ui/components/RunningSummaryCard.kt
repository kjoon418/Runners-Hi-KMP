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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import good.space.runnershi.ui.theme.RunnersHiTheme
import good.space.runnershi.util.format
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RunningSummaryCard(
    distanceKm: Double,
    runningTime: String,
    runningPace: String,
    totalTime: String,
    totalPace: String,
    calories: Int,
    earnedExp: Long? = null,
    modifier: Modifier = Modifier
) {
    val customColors = RunnersHiTheme.custom
    val colorScheme = RunnersHiTheme.colorScheme

    val backgroundBrush = Brush.horizontalGradient(
        colors = listOf(
            customColors.questDark,
            customColors.questLight
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            // 거리, 러닝 시간, 러닝 페이스
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // 거리
                SummaryItem(
                    label = "거리",
                    value = "%.2f".format(distanceKm),
                    unit = "km",
                    modifier = Modifier.weight(1f),
                    subContent = {
                        if (earnedExp != null && earnedExp > 0) {
                            Text(
                                text = "+ $earnedExp EXP",
                                style = RunnersHiTheme.typography.labelLarge.copy(
                                    color = customColors.resumeLight, // Green500
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                )

                // 러닝 시간
                SummaryItem(
                    label = "러닝 시간",
                    value = runningTime,
                    modifier = Modifier.weight(1f)
                )

                // 러닝 페이스
                SummaryItem(
                    label = "러닝 페이스",
                    value = runningPace,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = customColors.inputDisable
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 전체 시간, 전체 페이스, 소모 칼로리
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // 전체 시간
                SummaryItem(
                    label = "전체 시간",
                    value = totalTime,
                    isMainStat = false,
                    modifier = Modifier.weight(1f)
                )

                // 전체 페이스
                SummaryItem(
                    label = "전체 페이스",
                    value = totalPace,
                    isMainStat = false,
                    modifier = Modifier.weight(1f)
                )

                // 소모 칼로리
                SummaryItem(
                    label = "소모 칼로리",
                    value = "$calories",
                    unit = "kcal",
                    isMainStat = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 재사용 가능한 요약 아이템 컴포넌트
 */
@Composable
private fun SummaryItem(
    label: String,
    value: String,
    unit: String = "",
    isMainStat: Boolean = true,
    modifier: Modifier = Modifier,
    subContent: @Composable (() -> Unit)? = null
) {
    val labelColor = RunnersHiTheme.colorScheme.onSurfaceVariant
    val valueColor = RunnersHiTheme.colorScheme.onBackground

    val valueFontSize = if (isMainStat) 28.sp else 24.sp
    val unitFontSize = if (isMainStat) 16.sp else 14.sp

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Label
        Text(
            text = label,
            style = RunnersHiTheme.typography.bodyMedium,
            color = labelColor,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = valueFontSize,
                        color = valueColor
                    )
                ) {
                    append(value)
                }
                if (unit.isNotEmpty()) {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = unitFontSize,
                            color = valueColor
                        )
                    ) {
                        append(" $unit")
                    }
                }
            },
            textAlign = TextAlign.Center
        )

        // Sub Content (EXP 등)
        subContent?.invoke()
    }
}

@Preview
@Composable
private fun RunningSummaryCardPreview() {
    RunnersHiTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            RunningSummaryCard(
                distanceKm = 5.25,
                runningTime = "28:10",
                runningPace = "5'23''",
                totalTime = "30:15",
                totalPace = "5'45''",
                calories = 130,
                earnedExp = 50
            )
        }
    }
}
