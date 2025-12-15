package good.space.runnershi.viewmodel

import good.space.runnershi.auth.TokenStorage
import good.space.runnershi.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AppState {
    object Loading : AppState()
    object LoggedIn : AppState()
    object NeedsLogin : AppState()
}

class MainViewModel(
    private val tokenStorage: TokenStorage,
    private val apiClient: ApiClient
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _appState = MutableStateFlow<AppState>(AppState.Loading)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    init {
        checkLoginStatus()
        observeAuthErrors()
    }

    // [초기 실행] 토큰 확인
    private fun checkLoginStatus() {
        scope.launch {
            val token = tokenStorage.getAccessToken()
            if (token.isNullOrEmpty()) {
                _appState.value = AppState.NeedsLogin
            } else {
                _appState.value = AppState.LoggedIn
            }
        }
    }

    // [실시간 감지] 리프레시 토큰 만료로 인한 강제 로그아웃 감지
    private fun observeAuthErrors() {
        scope.launch {
            apiClient.authErrorFlow.collect {
                _appState.value = AppState.NeedsLogin
            }
        }
    }
    
    // 로그인 성공 시 호출
    fun onLoginSuccess() {
        _appState.value = AppState.LoggedIn
    }
}

