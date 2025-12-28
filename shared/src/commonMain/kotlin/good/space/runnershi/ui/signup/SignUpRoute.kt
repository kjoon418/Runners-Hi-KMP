package good.space.runnershi.ui.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import good.space.runnershi.ui.components.RunnersHiBackHandler
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SignUpRoute(
    navigateBack: () -> Unit,
    navigateToHome: () -> Unit,
    viewModel: SignUpViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel.sideEffect) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                SignUpSideEffect.NavigateBack -> navigateBack()
                SignUpSideEffect.NavigateToHome -> navigateToHome()
            }
        }
    }

    RunnersHiBackHandler(enabled = true) {
        viewModel.onBackClick()
    }

    SignUpScreen(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onPasswordCheckChange = viewModel::onPasswordCheckChange,
        onNameChange = viewModel::onNameChange,
        onCharacterSelect = viewModel::onCharacterSelect,
        onNextClick = viewModel::onNextClick,
        onSignUpClick = viewModel::onSignUpClick,
        validateEmail = viewModel::validateEmail,
        validatePassword = viewModel::validatePassword,
        validateName = viewModel::validateName,
        onErrorShown = viewModel::clearSignUpError
    )
}
