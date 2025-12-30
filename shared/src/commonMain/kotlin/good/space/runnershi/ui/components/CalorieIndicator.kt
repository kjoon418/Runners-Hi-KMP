package good.space.runnershi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import good.space.runnershi.ui.theme.RunnersHiTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun CalorieIndicator(
    calories: Int,
    modifier: Modifier = Modifier,
    limitMasWidth: Dp = 160.dp
) {
    val backgroundColor = RunnersHiTheme.custom.cardBackground.copy(alpha = 0.8f)
    val accentColor = RunnersHiTheme.custom.calory

    BoxWithConstraints(
        modifier = modifier.widthIn(max = limitMasWidth)
    ) {
        val isCompact = this.maxWidth < 140.dp

        // 상태에 따른 동적 수치 할당
        val horizontalPadding = if (isCompact) 12.dp else 20.dp
        val verticalPadding = if (isCompact) 8.dp else 12.dp
        val iconSize = if (isCompact) 36.dp else 52.dp
        val titleFontSize = if (isCompact) 8.sp else 12.sp
        val numberFontSize = if (isCompact) 16.sp else 24.sp
        val unitFontSize = if (isCompact) 8.sp else 10.sp
        val roundedCorner = if (isCompact) 18.dp else 26.dp

        Card(
            shape = RoundedCornerShape(roundedCorner),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 텍스트 영역 (가변)
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "소모 칼로리",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = titleFontSize,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = calories.toString(),
                        color = accentColor,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = numberFontSize,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Kcal",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = unitFontSize
                        ),
                        maxLines = 1
                    )
                }

                // 아이콘 영역
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = "Calories burned",
                    tint = accentColor,
                    modifier = Modifier.requiredSize(iconSize)
                )
            }
        }
    }
}

@Preview
@Composable
fun CalorieIndicatorResponsivePreview() {
    RunnersHiTheme {
        Column(
            modifier = Modifier
                .background(Color.LightGray)
                .padding(20.dp)
        ) {
            CalorieIndicator(calories = 450, limitMasWidth = 200.dp)

            Spacer(modifier = Modifier.height(20.dp))

            CalorieIndicator(
                calories = 450,
                modifier = Modifier.width(110.dp)
            )
        }
    }
}
