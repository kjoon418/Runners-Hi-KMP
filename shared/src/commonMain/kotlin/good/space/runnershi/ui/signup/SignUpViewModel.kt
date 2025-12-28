package good.space.runnershi.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import good.space.runnershi.auth.TokenStorage
import good.space.runnershi.model.domain.auth.AuthValidationResult
import good.space.runnershi.model.domain.auth.Sex
import good.space.runnershi.model.domain.auth.ValidateEmailUseCase
import good.space.runnershi.model.domain.auth.ValidatePasswordUseCase
import good.space.runnershi.model.dto.auth.LoginResponse
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

    val emailVerified: Boolean = false,
    val passwordVerified: Boolean = false,
    val nameVerified: Boolean = false,

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
        _uiState.update {
            it.copy(
                email = email.trim(),
                emailError = null,
                emailVerified = false
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            val newState = it.copy(
                password = password,
                passwordError = null
            )

            newState.copy(
                passwordVerified = newState.isPasswordReady()
            )
        }
    }

    fun onPasswordCheckChange(passwordCheck: String) {
        _uiState.update {
            val newState = it.copy(
                passwordCheck = passwordCheck,
                passwordCheckError = null
            )

            val error = newState.passwordCheckErrorMessage

            newState.copy(
                passwordCheckError = error,
                passwordVerified = newState.isPasswordReady()
            )
        }
    }

    fun onNameChange(name: String) {
        _uiState.update {
            it.copy(
                name = name.trim(),
                nameError = null,
                nameVerified = false
            )
        }
    }

    fun onCharacterSelect(sex: Sex) {
        _uiState.update { it.copy(characterSex = sex) }
    }

    fun onSignUpClick() {
        val currentState = _uiState.value

        if (currentState.isLoading) {
            return
        }

        if (checkValidationAndSetErrors(currentState)) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, signUpError = null) }

            try {
                val result = signUp()

                result.onSuccess { response ->
                    tokenStorage.saveTokens(response.accessToken, response.refreshToken)
                    _uiState.update { it.copy(isLoading = false) }
                    _sideEffect.send(SignUpSideEffect.NavigateToHome)
                }.onFailure { e ->
                    val errorMessage = e.message ?: "회원가입에 실패했습니다."

                    _uiState.update {
                        SignUpUiState(
                            currentStep = SignUpStep.EmailPassword,
                            signUpError = errorMessage,
                            isLoading = false
                        )
                    }
                }
            } catch (_: Exception) {
                _uiState.update {
                    SignUpUiState(
                        currentStep = SignUpStep.EmailPassword,
                        signUpError = "알 수 없는 오류가 발생했습니다. 다시 시도해주세요.",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onNextClick() {
        val currentState = _uiState.value

        if (currentState.currentStep == SignUpStep.EmailPassword) {
            if (currentState.isEmailNotVerified() || currentState.isPasswordNotVerified()) {
                println("Email not verified = ${currentState.isEmailNotVerified()}")
                println("Password not verified = ${currentState.isPasswordNotVerified()}")
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
        val email = currentState.email

        val emailValidation = validateEmailUseCase(email)
        if (emailValidation !is AuthValidationResult.Success) {
            val errorMsg = when (emailValidation) {
                AuthValidationResult.Error.Blank -> "이메일을 입력해주세요."
                AuthValidationResult.Error.InvalidFormat -> "올바른 이메일 형식이 아닙니다."
                else -> "이메일 오류"
            }
            _uiState.update { it.copy(emailError = errorMsg) }
            return
        }

        viewModelScope.launch {
            val result = authRepository.checkEmailAvailability(email)

            result.onSuccess { isAvailable ->
                if (isAvailable) {
                    _uiState.update {
                        it.copy(
                            emailError = null,
                            emailVerified = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            emailError = "이미 사용 중인 이메일입니다.",
                            emailVerified = false
                        )
                    }
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        emailError = "인터넷 연결을 확인해주세요.",
                        emailVerified = false
                    )
                }
            }
        }
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
        val name = currentState.name

        if (name.isEmpty()) {
            _uiState.update { it.copy(nameError = "이름이 비어 있습니다.") }
        }

        viewModelScope.launch {
            val result = authRepository.checkNameAvailability(name)

            result.onSuccess { isAvailable ->
                if (isAvailable) {
                    _uiState.update {
                        it.copy(
                            nameError = null,
                            nameVerified = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            nameError = "이미 사용 중인 이름입니다.",
                            nameVerified = false
                        )
                    }
                }
            }.onFailure {
                _uiState.update { it.copy(nameError = "인터넷 연결을 확인해주세요.") }
            }
        }
    }

    fun clearSignUpError() {
        _uiState.update { it.copy(signUpError = null) }
    }

    private suspend fun signUp(): Result<LoginResponse> {
        val currentState = _uiState.value

        return authRepository.signUp(currentState.toSignUpRequest())
    }

    private val SignUpUiState.passwordCheckErrorMessage: String?
        get() {
            if (password != passwordCheck) {
                return "비밀번호가 일치하지 않습니다."
            }

            return null
        }

    private fun checkValidationAndSetErrors(state: SignUpUiState): Boolean {
        var hasError = false
        var newState = state

        // 이메일 검사
        if (state.isEmailNotVerified()) {
            newState = newState.copy(emailError = "이메일을 확인해주세요.")
            hasError = true
        }

        // 비밀번호 검사
        if (state.isPasswordNotVerified()) {
            newState = newState.copy(passwordError = "비밀번호를 확인해주세요.")
            hasError = true
        }

        // 이름 검사
        if (state.isNameNotVerified()) {
            newState = newState.copy(nameError = "이름을 확인해주세요.")
            hasError = true
        }

        // 캐릭터 검사
        if (state.isCharacterNotSelected()) {
            newState = newState.copy(signUpError = "캐릭터를 선택해주세요.")
            hasError = true
        }

        if (hasError) {
            _uiState.value = newState
        }

        return hasError
    }

    private fun SignUpUiState.isEmailNotVerified(): Boolean {
        return !emailVerified
                || email.isBlank()
                || emailError != null
    }

    private fun SignUpUiState.isPasswordNotVerified(): Boolean {
        return !passwordVerified
                || password.isBlank()
                || passwordCheck.isBlank()
                || passwordError != null
                || passwordCheckError != null
    }

    private fun SignUpUiState.isNameNotVerified(): Boolean {
        return !nameVerified
                || name.isBlank()
    }

    private fun SignUpUiState.isCharacterNotSelected(): Boolean {
        return characterSex == null
    }

    private fun SignUpUiState.isPasswordReady(): Boolean {
        val validateResult = validatePasswordUseCase(password)

        return validateResult is AuthValidationResult.Success
                && passwordCheck == password
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
