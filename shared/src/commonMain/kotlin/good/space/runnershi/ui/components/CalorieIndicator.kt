package good.space.runnershi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import good.space.runnershi.ui.theme.RunnersHiTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun CalorieIndicator(
    calories: Int,
    modifier: Modifier = Modifier
) {
    val backgroundColor = RunnersHiTheme.custom.cardBackground.copy(alpha = 0.8f)
    val accentColor = RunnersHiTheme.custom.calory

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "소모 칼로리",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = calories.toString(),
                    color = accentColor,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Kcal",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = "Calories burned",
                tint = accentColor,
                modifier = Modifier.size(56.dp)
            )
        }
    }
}

@Preview
@Composable
fun CalorieIndicatorPreview() {
    RunnersHiTheme {
        Column(
            modifier = Modifier
                .background(Color.LightGray)
                .padding(20.dp)
        ) {
            CalorieIndicator(calories = 0)
            Spacer(modifier = Modifier.height(10.dp))
            CalorieIndicator(calories = 450)
        }
    }
}
