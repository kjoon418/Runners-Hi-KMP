package good.space.runnershi.ui.signup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import good.space.runnershi.model.domain.auth.Sex
import good.space.runnershi.ui.signup.steps.Step1Content
import good.space.runnershi.ui.signup.steps.Step2Content
import good.space.runnershi.ui.theme.RunnersHiTheme

@Composable
fun SignUpScreen(
    uiState: SignUpUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordCheckChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onCharacterSelect: (Sex) -> Unit,
    onNextClick: () -> Unit,
    onSignUpClick: () -> Unit,
    validateEmail: () -> Unit,
    validatePassword: () -> Unit,
    validateName: () -> Unit,
    onErrorShown: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.signUpError) {
        uiState.signUpError?.let { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
            onErrorShown()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RunnersHiTheme.colorScheme.background)
            // 상태 표시줄(Status Bar)만큼 패딩을 주어 겹치지 않게 함
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(100.dp))

            // 단계별 화면 전환 애니메이션
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    // 1단계 -> 2단계: 오른쪽에서 들어옴
                    if (targetState == SignUpStep.NameCharacter) {
                        (slideInHorizontally { it } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it } + fadeOut())
                    } else {
                        // 2단계 -> 1단계: 왼쪽에서 들어옴 (뒤로가기 느낌)
                        (slideInHorizontally { -it } + fadeIn()) togetherWith
                                (slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "SignUpStepAnimation"
            ) { step ->
                when (step) {
                    SignUpStep.EmailPassword -> {
                        Step1Content(
                            uiState = uiState,
                            onEmailChange = onEmailChange,
                            onPasswordChange = onPasswordChange,
                            onPasswordCheckChange = onPasswordCheckChange,
                            onNextClick = onNextClick,
                            validateEmail = validateEmail,
                            validatePassword = validatePassword
                        )
                    }
                    SignUpStep.NameCharacter -> {
                        Step2Content(
                            uiState = uiState,
                            onNameChange = onNameChange,
                            onCharacterSelect = onCharacterSelect,
                            onSignUpClick = onSignUpClick,
                            onValidateName = validateName
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.systemBars)
        )

        if (uiState.isLoading) {
            LoadingIndicator()
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = RunnersHiTheme.colorScheme.primary)
    }
}
