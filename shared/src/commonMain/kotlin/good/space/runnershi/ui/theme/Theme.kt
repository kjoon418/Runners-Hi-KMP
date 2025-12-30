package good.space.runnershi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    background = White300,
    onBackground = Gray900,

    primary = Blue700,
    onPrimary = White100,

    primaryContainer = White100,
    onPrimaryContainer = Gray900,

    surface = White300,
    onSurface = Gray900,
    onSurfaceVariant = Gray700,
    
    error = Red,
    onError = White100
)

data class RunnersHiCustomColors(
    val inputBorder: Color,
    val inputBorderOnFocused: Color,
    val inputLabel: Color,
    val inputDisable: Color,
    val inputDisableBorder: Color,

    val questLight: Color,
    val questDark: Color,
    val clearedQuestLight: Color,
    val clearedQuestDark: Color,

    val trophyLight: Color,
    val trophyDark: Color,

    val expProgressLight: Color,
    val expProgressMedium: Color,
    val expProgressDark: Color,

    val pauseLight: Color,
    val pauseDark: Color,

    val resumeLight: Color,
    val resumeDark: Color,

    val stopLight: Color,
    val stopDark: Color
)

val LightCustomColors = RunnersHiCustomColors(
    inputBorder = Blue300,
    inputBorderOnFocused = Blue700,
    inputLabel = Blue500,
    inputDisable = Gray300,
    inputDisableBorder = Gray700,

    questLight = SkyBlue50,
    questDark = SkyBlue100,
    clearedQuestLight = Green100,
    clearedQuestDark = Green300,

    trophyLight = Yellow50,
    trophyDark = Yellow300,

    expProgressLight = Indigo200,
    expProgressMedium = Indigo500,
    expProgressDark = Indigo700,

    pauseLight = Amber500,
    pauseDark = Amber600,

    resumeLight = Green500,
    resumeDark = Green600,

    stopLight = Gray600,
    stopDark = Gray800
)

val LocalRunnersHiCustomColors = staticCompositionLocalOf { LightCustomColors
}

@Composable
fun RunnersHiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), 
    content: @Composable () -> Unit
) {
    // 다크모드/라이트모드 미구현
    val colorScheme = LightColorScheme
    val customColors = LightCustomColors

    // MaterialTheme을 통해 앱 전체에 디자인 시스템 주입
    CompositionLocalProvider(
        LocalRunnersHiCustomColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

object RunnersHiTheme {
    val colorScheme: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val custom: RunnersHiCustomColors
        @Composable
        @ReadOnlyComposable
        get() = LocalRunnersHiCustomColors.current
}
