package good.space.runnershi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import good.space.runnershi.ui.theme.RunnersHiTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RunnersHiTextField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    errorMessage: String? = null, // 에러 메시지가 null이 아니라면 에러가 발생한 것으로 판단
    enabled: Boolean = true,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    onValidate: (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        if (title.isNotEmpty()) {
            Label(title = title)
            Spacer(modifier = Modifier.height(8.dp))
        }

        InputField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            enabled = enabled,
            singleLine = singleLine,
            isError = errorMessage != null,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    // 이전에 포커스가 있었고 지금 없어졌다면, 검증 실행
                    if (isFocused && !focusState.isFocused) {
                        onValidate?.invoke()
                    }
                    isFocused = focusState.isFocused
                }
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            ErrorMessage(errorMessage)
        }
    }
}

@Composable
private fun Label(
    title: String
) {
    Text(
        text = title,
        style = RunnersHiTheme.typography.labelLarge,
        color = RunnersHiTheme.custom.inputLabel,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    singleLine: Boolean,
    isError: Boolean,
    visualTransformation: VisualTransformation,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    modifier: Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        textStyle = RunnersHiTheme.typography.bodyLarge,

        placeholder = {
            Text(
                text = placeholder,
                style = RunnersHiTheme.typography.bodyLarge,
                color = RunnersHiTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },

        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(12.dp),

        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = RunnersHiTheme.custom.inputBorderOnFocused,
            unfocusedBorderColor = RunnersHiTheme.custom.inputBorder,
            errorBorderColor = RunnersHiTheme.colorScheme.error,

            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )
    )
}

@Composable
private fun ErrorMessage(
    errorMessage: String
) {
    Text(
        text = errorMessage,
        style = RunnersHiTheme.typography.labelSmall,
        color = RunnersHiTheme.colorScheme.error
    )
}

@Preview
@Composable
private fun RunnersHiTextFieldPreview() {
    RunnersHiTheme {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            // 기본 상태
            RunnersHiTextField(
                title = "Email",
                value = "",
                onValueChange = {},
                placeholder = "이메일을 입력해주세요"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 입력된 상태
            RunnersHiTextField(
                title = "Email",
                value = "Hello@world.com",
                onValueChange = {}
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 에러 상태
            RunnersHiTextField(
                title = "Email",
                value = "hello world!",
                onValueChange = {},
                errorMessage = "이메일 형식이 부적절합니다."
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 비밀번호 입력
            RunnersHiTextField(
                title = "Password",
                value = "helloPassword!",
                onValueChange = {},
                visualTransformation = PasswordVisualTransformation()
            )
        }
    }
}
