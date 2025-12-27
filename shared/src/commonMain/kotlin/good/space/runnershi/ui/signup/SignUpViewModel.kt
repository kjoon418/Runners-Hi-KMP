package good.space.runnershi.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import good.space.runnershi.auth.TokenStorage
import good.space.runnershi.model.domain.auth.AuthValidationResult
import good.space.runnershi.model.domain.auth.Sex
import good.space.runnershi.model.domain.auth.ValidateEmailUseCase
import good.space.runnershi.model.domain.auth.ValidatePasswordUseCase
import good.space.runnershi.model.dto.auth.SignUpRequest
import good.space.runnershi.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SignUpStep {
    EmailPassword,
    NameCharacter
}
data class SignUpUiState(
    val currentStep: SignUpStep = SignUpStep.EmailPassword,

    val email: String = "",
    val password: String = "",
    val passwordCheck: String = "",
    val name: String = "",
    val characterSex: Sex? = null,

    val emailError: String? = null,
    val passwordError: String? = null,
    val passwordCheckError: String? = null,
    val nameError: String? = null,
    val signUpError: String? = null, // 서버 통신 에러 등 전체 에러

    val isLoading: Boolean = false
)

sealed interface SignUpSideEffect {
    data object NavigateToHome : SignUpSideEffect // 회원가입+로그인 성공 시
    data object NavigateBack : SignUpSideEffect   // 뒤로 가기 버튼 클릭 시
}

class SignUpViewModel(
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = Channel<SignUpSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onPasswordCheckChange(passwordCheck: String) {
        _uiState.update {
            val newState = it.copy(passwordCheck = passwordCheck)

            val error = newState.passwordCheckErrorMessage

            newState.copy(passwordCheckError = error)
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onCharacterSelect(sex: Sex) {
        _uiState.update { it.copy(characterSex = sex) }
    }

    fun onSignUpClick() {
        val currentState = _uiState.value

        if (currentState.isLoading) {
            return
        }

        if (currentState.hasEmailPasswordError()
            || currentState.hasNameCharacterError()) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, signUpError = null) }

            try {
                signUp()
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        signUpError = "알 수 없는 오류가 발생했습니다."
                    )
                }
            }
        }
    }

    fun onNextClick() {
        val currentState = _uiState.value

        if (currentState.currentStep == SignUpStep.EmailPassword) {
            if (currentState.hasEmailPasswordError()) {
                return
            }

            // 통과하면 다음 단계로
            _uiState.update { it.copy(currentStep = SignUpStep.NameCharacter) }
        }
    }

    fun onBackClick() {
        val currentState = _uiState.value

        when (currentState.currentStep) {
            // 2단계: 1단계로 이동
            SignUpStep.NameCharacter -> {
                _uiState.update { it.copy(currentStep = SignUpStep.EmailPassword) }
            }
            // 1단계: 화면 종료
            SignUpStep.EmailPassword -> {
                viewModelScope.launch {
                    _sideEffect.send(SignUpSideEffect.NavigateBack)
                }
            }
        }
    }

    fun validateEmail() {
        val currentState = _uiState.value

        val emailValidation = validateEmailUseCase(currentState.email)
        if (emailValidation !is AuthValidationResult.Success) {
            val errorMsg = when (emailValidation) {
                AuthValidationResult.Error.Blank -> "이메일을 입력해주세요."
                AuthValidationResult.Error.InvalidFormat -> "올바른 이메일 형식이 아닙니다."
                else -> "이메일 오류"
            }
            _uiState.update { it.copy(emailError = errorMsg) }
            return
        }

        // TODO: 서버로 이메일 중복 확인 요청
    }

    fun validatePassword() {
        val currentState = _uiState.value

        val passwordValidation = validatePasswordUseCase(currentState.password)
        if (passwordValidation !is AuthValidationResult.Success) {
            _uiState.update { it.copy(passwordError = "비밀번호는 6자 이상이어야 합니다.") }
        }
    }

    fun validateName() {
        val currentState = _uiState.value

        // TODO: 서버로 이름 중복 확인 요청
    }

    private suspend fun signUp() {
        val currentState = _uiState.value

        val result = authRepository.signUp(currentState.toSignUpRequest())

        result.onSuccess { response ->
            tokenStorage.saveTokens(response.accessToken, response.refreshToken)
            _uiState.update { it.copy(isLoading = false) }
            _sideEffect.send(SignUpSideEffect.NavigateToHome)
        }.onFailure { e ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    signUpError = e.message ?: "회원가입에 실패했습니다."
                )
            }
        }
    }

    private val SignUpUiState.passwordCheckErrorMessage: String?
        get() {
            if (password != passwordCheck) {
                return "비밀번호가 일치하지 않습니다."
            }

            return null
        }

    private fun SignUpUiState.hasEmailPasswordError(): Boolean {
        return emailError != null
                || passwordError != null
                || passwordCheckError != null
                || email.isBlank()
                || password.isBlank()
    }

    private fun SignUpUiState.hasNameCharacterError(): Boolean {
        return nameError != null
                || name.isBlank()
                || characterSex == null
    }

    private fun SignUpUiState.toSignUpRequest(): SignUpRequest {
        return SignUpRequest(
            email = email,
            password = password,
            name = name,
            sex = characterSex!!
        )
    }
}
