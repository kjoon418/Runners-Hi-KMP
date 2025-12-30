package good.space.runnershi.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import good.space.runnershi.ui.components.ButtonStyle
import good.space.runnershi.ui.components.Logo
import good.space.runnershi.ui.components.RunnersHiButton
import good.space.runnershi.ui.components.RunnersHiTextField
import good.space.runnershi.ui.theme.RunnersHiTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LoginScreen(
    email: String,
    password: String,
    emailError: String? = null,
    passwordError: String? = null,
    loginError: String? = null,
    isLoading: Boolean = false,

    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,

    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RunnersHiTheme.colorScheme.background)
            // 배경 터치 시 포커스 해제는 최상위 Box에서 처리하는 것이 깔끔함
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(RunnersHiTheme.colorScheme.background)
                .padding(horizontal = 24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusManager.clearFocus() },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Logo(400.dp)

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
            ) {
                EmailInput(email, onEmailChange, emailError)

                Spacer(modifier = Modifier.height(6.dp))

                PasswordInput(password, onPasswordChange, passwordError)

                LoginError(loginError)

                Spacer(modifier = Modifier.height(24.dp))

                LoginButton(onLoginClick)

                Spacer(modifier = Modifier.height(12.dp))

                SignUpButton(onSignUpClick)
            }
        }

        LoadingIndicator(
            isLoading = isLoading,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun EmailInput(
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String?
) {
    RunnersHiTextField(
        title = "Email",
        value = email,
        onValueChange = onEmailChange,
        placeholder = "이메일을 입력해주세요",
        errorMessage = emailError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )
}

@Composable
private fun PasswordInput(
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordError: String?
) {
    RunnersHiTextField(
        title = "Password",
        value = password,
        onValueChange = onPasswordChange,
        placeholder = "비밀번호를 입력해주세요",
        errorMessage = passwordError,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
}

@Composable
private fun LoginError(
    loginError: String?
) {
    if (loginError == null) {
        return
    }

    Text(
        text = loginError,
        color = RunnersHiTheme.colorScheme.error,
        style = RunnersHiTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun LoginButton(
    onClick: () -> Unit
) {
    RunnersHiButton(
        text = "Log In",
        onClick = onClick,
        style = ButtonStyle.FILLED
    )
}

@Composable
private fun SignUpButton(
    onClick: () -> Unit
) {
    RunnersHiButton(
        text = "Sign Up",
        onClick = onClick,
        style = ButtonStyle.OUTLINED
    )
}

@Composable
private fun LoadingIndicator(
    isLoading: Boolean,
    modifier: Modifier
) {
    if (!isLoading) {
        return
    }

    CircularProgressIndicator(
        modifier = modifier,
        color = RunnersHiTheme.colorScheme.primary
    )
}

@Preview
@Composable
private fun LoginScreenPreview() {
    RunnersHiTheme {
        // 프리뷰를 위한 가짜 상태 (Fake State)
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        // 간단한 에러 로직 시뮬레이션
        val emailError = if (email.isNotEmpty() && !email.contains("@")) "이메일 형식이 아닙니다." else null

        LoginScreen(
            email = email,
            password = password,
            emailError = emailError,
            passwordError = null,
            loginError = "이메일과 비밀번호를 확인해주세요",
            isLoading = true,
            onEmailChange = { email = it },
            onPasswordChange = { password = it },
            onLoginClick = { /* 로그인 로직 수행 */ },
            onSignUpClick = { /* 회원가입 화면 이동 */ }
        )
    }
}
