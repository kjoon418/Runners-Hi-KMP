package good.space.runnershi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import good.space.runnershi.database.LocalRunningDataSource
import good.space.runnershi.service.AndroidServiceController
import good.space.runnershi.shared.di.androidPlatformModule
import good.space.runnershi.shared.di.initKoin
import good.space.runnershi.ui.screen.RunResultScreen
import good.space.runnershi.ui.screen.RunningScreen
import good.space.runnershi.viewmodel.RunningViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        initKoin(extraModules = listOf(androidPlatformModule))
        
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // 1. 의존성 주입 (수동)
        val serviceController = AndroidServiceController(this)
        val viewModel = RunningViewModel(serviceController)
        
        // 2. 앱 시작 시 복구 로직 실행 (사용자가 앱 아이콘을 눌러서 켤 때)
        val dbSource = LocalRunningDataSource(this)
        lifecycleScope.launch {
            if (dbSource.recoverLastRunIfAny()) {
                // 복구되었다면, '일시정지' 상태로 UI가 그려질 것임.
                // 사용자에게 스낵바나 다이얼로그로 "이전 기록을 불러왔습니다" 라고 알려주면 Best.
            }
        }

        setContent {
            MaterialTheme {
                // 3. 화면 전환 처리
                AppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AppContent(viewModel: RunningViewModel) {
    val runResult by viewModel.runResult.collectAsState()

    // runResult 데이터가 있으면 결과 화면을, 없으면 러닝 화면을 보여줌
    if (runResult != null) {
        RunResultScreen(
            result = runResult!!,
            onClose = { viewModel.closeResultScreen() }
        )
    } else {
        RunningScreen(viewModel = viewModel)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    MaterialTheme {
        // Preview용 더미 ViewModel (실제로는 사용하지 않음)
    }
}