package good.space.runnershi.ui.signup.steps

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import good.space.runnershi.ui.components.ButtonStyle
import good.space.runnershi.ui.components.RunnersHiButton
import good.space.runnershi.ui.components.RunnersHiTextField
import good.space.runnershi.ui.signup.SignUpUiState
import good.space.runnershi.ui.theme.RunnersHiTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import runnershi.shared.generated.resources.Res
import runnershi.shared.generated.resources.logo_signup

@Composable
fun Step1Content(
    uiState: SignUpUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordCheckChange: (String) -> Unit,
    onNextClick: () -> Unit,
    validateEmail: () -> Unit,
    validatePassword: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() }
    ) {
        Logo()
        Spacer(modifier = Modifier.height(24.dp))

        EmailInput(uiState, onEmailChange, validateEmail)
        Spacer(modifier = Modifier.height(24.dp))

        PasswordInput(uiState, onPasswordChange, validatePassword)
        Spacer(modifier = Modifier.height(24.dp))

        PasswordCheckInput(uiState, onPasswordCheckChange)
        Spacer(modifier = Modifier.height(24.dp))

        NextButton(onNextClick)
    }
}

@Composable
private fun Logo() {
    Image(
        painter = painterResource(Res.drawable.logo_signup),
        contentDescription = "Runners Hi Logo",
        modifier = Modifier.width(400.dp)
    )
}

@Composable
private fun EmailInput(
    uiState: SignUpUiState,
    onValueChange: (String) -> Unit,
    onValidate: () -> Unit
) {
    RunnersHiTextField(
        title = "Email",
        value = uiState.email,
        onValueChange = onValueChange,
        placeholder = "이메일을 입력해주세요",
        errorMessage = uiState.emailError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        onValidate = onValidate
    )
}

@Composable
private fun PasswordInput(
    uiState: SignUpUiState,
    onValueChange: (String) -> Unit,
    onValidate: () -> Unit
) {
    RunnersHiTextField(
        title = "Password",
        value = uiState.password,
        onValueChange = onValueChange,
        placeholder = "비밀번호를 입력해주세요",
        errorMessage = uiState.passwordError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        ),
        onValidate = onValidate
    )
}

@Composable
private fun PasswordCheckInput(
    uiState: SignUpUiState,
    onValueChange: (String) -> Unit
) {
    RunnersHiTextField(
        title = "Password Check",
        value = uiState.passwordCheck,
        onValueChange = onValueChange,
        placeholder = "비밀번호를 다시 입력해주세요",
        errorMessage = uiState.passwordCheckError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        )
    )
}

@Composable
private fun NextButton(
    onClick: () -> Unit
) {
    RunnersHiButton(
        text = "시작하기",
        onClick = onClick,
        style = ButtonStyle.FILLED,
        modifier = Modifier.padding(bottom = 24.dp)
    )
}

@Preview
@Composable
private fun Step1ContentPreview() {
    RunnersHiTheme {
        Step1Content(
            uiState = SignUpUiState(
                email = "test@example.com",
                password = "password123"
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onPasswordCheckChange = {},
            onNextClick = {},
            validateEmail = {},
            validatePassword = {}
        )
    }
}
