package good.space.runnershi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import good.space.runnershi.auth.AndroidTokenStorage
import good.space.runnershi.database.LocalRunningDataSource
import good.space.runnershi.network.ApiClient
import good.space.runnershi.repository.AuthRepositoryImpl
import good.space.runnershi.repository.MockRunRepository
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
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Koin이 이미 시작되어 있다면 재시작하지 않음 (브랜치 전환 후 pull 등)
        if (GlobalContext.getOrNull() == null) {
        initKoin(extraModules = listOf(androidPlatformModule))
        }
        
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // 1. BuildConfig에서 BASE_URL 읽기 (Gradle에서 local.properties 값을 주입)
        val baseUrl = BuildConfig.BASE_URL

        // 2. 의존성 주입 (수동)
        val tokenStorage = AndroidTokenStorage(this)
        
        // 3. AuthRepository용 HttpClient 생성 (인증 없이 사용)
        val authHttpClient = HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                })
            }
        }
        val authRepository = AuthRepositoryImpl(authHttpClient, baseUrl)
        
        // 4. ApiClient 생성 (인증 플러그인 포함)
        val apiClient = ApiClient(tokenStorage, baseUrl)
        val serviceController = AndroidServiceController(this)

        // 5. RunRepository 생성
        // TODO: 서버 API가 준비되면 RunRepositoryImpl(apiClient)로 변경
        val runRepository = MockRunRepository() // 테스트용 Mock 데이터 사용
        val runningViewModel = RunningViewModel(serviceController, runRepository)
        val mainViewModel = MainViewModel(tokenStorage, apiClient)
        val loginViewModel = LoginViewModel(authRepository, tokenStorage)
        val signUpViewModel = SignUpViewModel(authRepository, tokenStorage)
        
        // 2. DB 및 ServiceController 준비
        val dbSource = LocalRunningDataSource(this)
        
        // 3. 로그아웃 시 DB 데이터 삭제 및 서비스 중지 콜백 설정
        mainViewModel.onLogoutCallback = {
            // 1. 서비스를 먼저 중지 (위치 추적 및 DB 저장 중단)
            serviceController.stopService()
            // 2. 미완료 러닝 데이터 삭제
            dbSource.discardRun()
        }

        setContent {
            MaterialTheme {
                AppRoot(
                    mainViewModel = mainViewModel,
                    loginViewModel = loginViewModel,
                    signUpViewModel = signUpViewModel,
                    runningViewModel = runningViewModel,
                    dbSource = dbSource,
                    serviceController = serviceController
                )
        }
        }
    }
}

// 복구 다이얼로그 래퍼
@Composable
fun RecoveryDialogWrapper(
    dbSource: LocalRunningDataSource,
    serviceController: AndroidServiceController,
    content: @Composable () -> Unit
) {
    var showRecoveryDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 앱 시작 시 1회 체크
    LaunchedEffect(Unit) {
        if (dbSource.hasUnfinishedRun()) {
            showRecoveryDialog = true
        }
    }

    // 복구 다이얼로그
    if (showRecoveryDialog) {
        AlertDialog(
            onDismissRequest = {
                // 다이얼로그 바깥 터치 시 '아니요'와 동일하게 처리
                scope.launch {
                    dbSource.discardRun()
                    showRecoveryDialog = false
                }
            },
            icon = { 
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null
                ) 
            },
            title = { Text("비정상 종료 감지") },
            text = { Text("이전 러닝 기록이 남아있습니다. 이어서 달리시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            // 1. 데이터 복구 (메모리로 로드)
                            val success = dbSource.restoreRun()
                            if (success) {
                                // 2. 서비스 재시작 (알림 표시 & 상태 유지)
                                // 주의: RESUME 액션을 보내야 함
                                serviceController.resumeService()
                            }
                            showRecoveryDialog = false
                        }
                    }
                ) {
                    Text("네, 이어서 할게요")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            // 1. 데이터 영구 삭제
                            dbSource.discardRun()
                            showRecoveryDialog = false
                        }
                    }
                ) {
                    Text("아니요, 새로 할게요", color = Color.Red)
                }
            }
        )
    }

    // 메인 화면
    content()
}

// 간단한 네비게이션 상태 정의
private enum class AuthScreenType { Login, SignUp }

@Composable
fun AppRoot(
    mainViewModel: MainViewModel,
    loginViewModel: LoginViewModel,
    signUpViewModel: SignUpViewModel,
    runningViewModel: RunningViewModel,
    dbSource: LocalRunningDataSource,
    serviceController: AndroidServiceController
) {
    val appState by mainViewModel.appState.collectAsState()

    when (appState) {
        is AppState.Loading -> {
            CircularProgressIndicator()
        }
        is AppState.NeedsLogin -> {
            // 로그아웃 후 로그인 화면으로 돌아왔을 때 ViewModel 상태 리셋
            LaunchedEffect(appState) {
                if (appState is AppState.NeedsLogin) {
                    loginViewModel.resetLoginSuccess()
                    signUpViewModel.resetSignUpSuccess()
                }
            }
            AuthFlow(
                loginViewModel = loginViewModel,
                signUpViewModel = signUpViewModel,
                onAuthSuccess = { mainViewModel.onLoginSuccess() }
            )
        }
        is AppState.LoggedIn -> {
            // 로그인 후에만 복구 다이얼로그 표시
            RecoveryDialogWrapper(
                dbSource = dbSource,
                serviceController = serviceController,
                content = {
                    AppContent(runningViewModel, mainViewModel)
                }
            )
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
fun AppContent(
    viewModel: RunningViewModel,
    mainViewModel: MainViewModel
) {
    val runResult by viewModel.runResult.collectAsState()

    if (runResult != null) {
        RunResultScreen(
            result = runResult!!,
            onClose = { viewModel.closeResultScreen() }
        )
    } else {
        RunningScreen(
            viewModel = viewModel,
            mainViewModel = mainViewModel
        )
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    MaterialTheme {
        // Preview용 더미 ViewModel (실제로는 사용하지 않음)
    }
}
