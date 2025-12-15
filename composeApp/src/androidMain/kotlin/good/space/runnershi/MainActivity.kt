package good.space.runnershi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import good.space.runnershi.auth.AndroidTokenStorage
import good.space.runnershi.database.LocalRunningDataSource
import good.space.runnershi.network.ApiClient
import good.space.runnershi.repository.MockAuthRepository
import good.space.runnershi.service.AndroidServiceController
import good.space.runnershi.shared.di.androidPlatformModule
import good.space.runnershi.shared.di.initKoin
import good.space.runnershi.ui.screen.LoginScreen
import good.space.runnershi.ui.screen.RunResultScreen
import good.space.runnershi.ui.screen.RunningScreen
import good.space.runnershi.ui.screen.SignUpScreen
import good.space.runnershi.viewmodel.AppState
import good.space.runnershi.viewmodel.LoginViewModel
import good.space.runnershi.viewmodel.MainViewModel
import good.space.runnershi.viewmodel.RunningViewModel
import good.space.runnershi.viewmodel.SignUpViewModel
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Koin이 이미 시작되어 있다면 재시작하지 않음 (브랜치 전환 후 pull 등)
        if (GlobalContext.getOrNull() == null) {
            initKoin(extraModules = listOf(androidPlatformModule))
        }
        
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // 1. 의존성 주입 (수동)
        val tokenStorage = AndroidTokenStorage(this)
        val authRepository = MockAuthRepository() // 서버 준비 전 Mock 사용
        val apiClient = ApiClient(tokenStorage)
        val serviceController = AndroidServiceController(this)

        val runningViewModel = RunningViewModel(serviceController)
        val mainViewModel = MainViewModel(tokenStorage, apiClient)
        val loginViewModel = LoginViewModel(authRepository, tokenStorage)
        val signUpViewModel = SignUpViewModel(authRepository, tokenStorage)
        
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
                AppRoot(
                    mainViewModel = mainViewModel,
                    loginViewModel = loginViewModel,
                    signUpViewModel = signUpViewModel,
                    runningViewModel = runningViewModel
                )
            }
        }
    }
}

// 간단한 네비게이션 상태 정의
private enum class AuthScreenType { Login, SignUp }

@Composable
fun AppRoot(
    mainViewModel: MainViewModel,
    loginViewModel: LoginViewModel,
    signUpViewModel: SignUpViewModel,
    runningViewModel: RunningViewModel
) {
    val appState by mainViewModel.appState.collectAsState()

    when (appState) {
        is AppState.Loading -> {
            CircularProgressIndicator()
        }
        is AppState.NeedsLogin -> {
            AuthFlow(
                loginViewModel = loginViewModel,
                signUpViewModel = signUpViewModel,
                onAuthSuccess = { mainViewModel.onLoginSuccess() }
            )
        }
        is AppState.LoggedIn -> {
            AppContent(runningViewModel)
        }
        else -> {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun AuthFlow(
    loginViewModel: LoginViewModel,
    signUpViewModel: SignUpViewModel,
    onAuthSuccess: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(AuthScreenType.Login) }

    when (currentScreen) {
        AuthScreenType.Login -> {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = onAuthSuccess,
                onNavigateToSignUp = { currentScreen = AuthScreenType.SignUp }
            )
        }
        AuthScreenType.SignUp -> {
            SignUpScreen(
                viewModel = signUpViewModel,
                onSignUpSuccess = onAuthSuccess,
                onBackClick = { currentScreen = AuthScreenType.Login }
            )
        }
    }
}

@Composable
fun AppContent(viewModel: RunningViewModel) {
    val runResult by viewModel.runResult.collectAsState()

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