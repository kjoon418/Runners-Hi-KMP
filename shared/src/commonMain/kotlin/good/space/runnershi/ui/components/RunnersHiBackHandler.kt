package good.space.runnershi.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun RunnersHiBackHandler(enabled: Boolean = true, onBack: () -> Unit)
