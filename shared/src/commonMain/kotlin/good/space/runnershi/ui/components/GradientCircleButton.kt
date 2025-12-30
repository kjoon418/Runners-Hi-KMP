package good.space.runnershi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import good.space.runnershi.ui.theme.Blue500
import good.space.runnershi.ui.theme.Blue700
import good.space.runnershi.ui.theme.RunnersHiTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class GradientCircleButtonColor {
    BLUE, GREEN, YELLOW, BLACK
}

enum class GradientCircleButtonIcon {
    START,
    STOP,
    PAUSE
}

@Composable
fun GradientCircleButton(
    buttonColor: GradientCircleButtonColor,
    buttonIcon: GradientCircleButtonIcon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val (lightColor, darkColor) = getColorsForButton(buttonColor)
    val iconVector = getIconForButton(buttonIcon)

    val elevation = if (isPressed) 0.dp else 6.dp

    // 배경 브러시
    val backgroundBrush = if (isPressed) {
        SolidColor(darkColor)
    } else {
        Brush.linearGradient(
            colors = listOf(lightColor, darkColor),
            start = Offset.Zero,
            end = Offset.Infinite
        )
    }

    // 테두리 광원 브러시
    val borderBrush = if (isPressed) {
        SolidColor(Color.Transparent)
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.5f),
                Color.Transparent
            ),
            start = Offset.Zero,
            end = Offset.Infinite
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = elevation,
                shape = CircleShape,
                clip = false,
                spotColor = darkColor.copy(alpha = 0.6f),
                ambientColor = darkColor.copy(alpha = 0.3f)
            )
            .clip(CircleShape)
            .background(brush = backgroundBrush)
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size / 2)
        )
    }
}

@Composable
@ReadOnlyComposable
private fun getColorsForButton(color: GradientCircleButtonColor): Pair<Color, Color> {
    return when (color) {
        GradientCircleButtonColor.BLUE -> {
            Pair(Blue500, Blue700)
        }
        GradientCircleButtonColor.GREEN -> {
            Pair(RunnersHiTheme.custom.resumeLight, RunnersHiTheme.custom.resumeDark)
        }
        GradientCircleButtonColor.YELLOW -> {
            Pair(RunnersHiTheme.custom.pauseLight, RunnersHiTheme.custom.pauseDark)
        }
        GradientCircleButtonColor.BLACK -> {
            Pair(RunnersHiTheme.custom.stopLight, RunnersHiTheme.custom.stopDark)
        }
    }
}

private fun getIconForButton(icon: GradientCircleButtonIcon): ImageVector {
    return when (icon) {
        GradientCircleButtonIcon.START -> Icons.Filled.PlayArrow
        GradientCircleButtonIcon.STOP -> Icons.Filled.Stop
        GradientCircleButtonIcon.PAUSE -> Icons.Filled.Pause
    }
}

@Preview
@Composable
private fun GradientCircleButtonPreview() {
    RunnersHiTheme {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 파란색 시작 버튼
            GradientCircleButton(
                buttonColor = GradientCircleButtonColor.BLUE,
                buttonIcon = GradientCircleButtonIcon.START,
                onClick = {}
            )

            // 2. 노란색 일시정지 버튼
            GradientCircleButton(
                buttonColor = GradientCircleButtonColor.YELLOW,
                buttonIcon = GradientCircleButtonIcon.PAUSE,
                onClick = {}
            )

            // 3. 초록색 시작 버튼
            GradientCircleButton(
                buttonColor = GradientCircleButtonColor.GREEN,
                buttonIcon = GradientCircleButtonIcon.START,
                onClick = {}
            )

            // 4. 검은색 정지 버튼
            GradientCircleButton(
                buttonColor = GradientCircleButtonColor.BLACK,
                buttonIcon = GradientCircleButtonIcon.STOP,
                onClick = {}
            )
        }
    }
}
