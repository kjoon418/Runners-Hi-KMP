package good.space.runnershi.viewmodel

import good.space.runnershi.auth.TokenStorage
import good.space.runnershi.model.dto.SignUpRequest
import good.space.runnershi.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignUpUiState(
    val email: String = "",
    val nickname: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSignUpSuccess: Boolean = false
)

class SignUpViewModel(
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, errorMessage = null) }
    fun onNicknameChange(v: String) = _uiState.update { it.copy(nickname = v, errorMessage = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, errorMessage = null) }
    fun onConfirmPasswordChange(v: String) = _uiState.update { it.copy(confirmPassword = v, errorMessage = null) }

    fun signUp() {
        val state = _uiState.value

        if (state.email.isBlank() || state.nickname.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "모든 항목을 입력해주세요.") }
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "비밀번호가 일치하지 않습니다.") }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "비밀번호는 6자 이상이어야 합니다.") }
            return
        }

        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = authRepository.signUp(
                SignUpRequest(state.email, state.password, state.nickname)
            )

            result.onSuccess { response ->
                tokenStorage.saveTokens(response.accessToken, response.refreshToken)
                _uiState.update { it.copy(isLoading = false, isSignUpSuccess = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "회원가입 실패") }
            }
        }
    }
}

