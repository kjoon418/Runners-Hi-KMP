package good.space.runnershi.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeRoute(
    navigateToRun: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showTtlDialog by remember { mutableStateOf(false) }

    HomeScreen(
        uiState = uiState,
        navigateToRun = navigateToRun,
        onSettingsClick = { showSettingsDialog = true },
        onTtlClick = { showTtlDialog = true },
        settingsDialog = {
            if (showSettingsDialog) {
                /* TODO: 설정 버튼을 클릭했을 때 띄울 다이얼로그 */
            }
        },
        ttlDialog = {
            if (showTtlDialog) {
                /* TODO: TTL 설정 버튼을 클릭했을 때 띄울 다이얼로그 */
            }
        }
    )
}
