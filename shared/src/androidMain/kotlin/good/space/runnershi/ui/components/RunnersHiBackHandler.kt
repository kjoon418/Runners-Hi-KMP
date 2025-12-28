package good.space.runnershi.ui.components

import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler

@Composable
actual fun RunnersHiBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // 안드로이드의 실제 하드웨어 뒤로가기 버튼 핸들러 연결
    BackHandler(enabled = enabled, onBack = onBack)
}
