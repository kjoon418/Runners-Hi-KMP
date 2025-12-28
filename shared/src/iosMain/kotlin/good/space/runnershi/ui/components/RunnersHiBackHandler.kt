package good.space.runnershi.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun RunnersHiBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS는 하드웨어 뒤로가기 버튼이 없으므로 아무것도 하지 않음.
}
