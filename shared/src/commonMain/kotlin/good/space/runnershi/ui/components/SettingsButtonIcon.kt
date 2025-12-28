package good.space.runnershi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import good.space.runnershi.ui.theme.Blue300
import good.space.runnershi.ui.theme.Blue700
import good.space.runnershi.ui.theme.Gray900
import good.space.runnershi.ui.theme.RunnersHiTheme
import good.space.runnershi.ui.theme.SkyBlue100
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class SettingsButtonIcon {
    SETTINGS,
    VOLUME_UP
}

@Composable
fun SettingsCircleButton(
    buttonIcon: SettingsButtonIcon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val iconVector = getIconForSettingsButton(buttonIcon)

    val elevation = if (isPressed) 0.dp else 4.dp

    // 배경색
    val backgroundColor = if (isPressed) {
        Blue300
    } else {
        SkyBlue100
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = elevation,
                shape = CircleShape,
                clip = false,
                spotColor = Gray900.copy(alpha = 0.3f),
                ambientColor = Gray900.copy(alpha = 0.2f)
            )
            .clip(CircleShape)
            .background(color = backgroundColor)
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
            tint = Blue700, // 아이콘 색상 (진한 파랑)
            modifier = Modifier.size(size / 2)
        )
    }
}

private fun getIconForSettingsButton(icon: SettingsButtonIcon): ImageVector {
    return when (icon) {
        SettingsButtonIcon.SETTINGS -> Icons.Filled.Settings
        SettingsButtonIcon.VOLUME_UP -> Icons.AutoMirrored.Filled.VolumeUp
    }
}

@Preview
@Composable
private fun SettingsCircleButtonPreview() {
    RunnersHiTheme {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsCircleButton(
                buttonIcon = SettingsButtonIcon.SETTINGS,
                onClick = {}
            )

            SettingsCircleButton(
                buttonIcon = SettingsButtonIcon.VOLUME_UP,
                onClick = {}
            )
        }
    }
}
