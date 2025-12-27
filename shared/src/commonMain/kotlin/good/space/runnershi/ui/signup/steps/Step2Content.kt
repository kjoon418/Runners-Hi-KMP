package good.space.runnershi.ui.signup.steps

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import good.space.runnershi.model.domain.auth.Sex
import good.space.runnershi.ui.components.ButtonStyle
import good.space.runnershi.ui.components.RunnersHiButton
import good.space.runnershi.ui.components.RunnersHiTextField
import good.space.runnershi.ui.signup.SignUpUiState
import good.space.runnershi.ui.theme.RunnersHiTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

// 내부 로직용 Enum (외부 노출 불필요)
private enum class Step2Focus {
    Name, Character
}

@Composable
fun Step2Content(
    uiState: SignUpUiState,
    onNameChange: (String) -> Unit,
    onValidateName: () -> Unit,
    onCharacterSelect: (Sex) -> Unit,
    onSignUpClick: () -> Unit
) {
    var focusMode by remember { mutableStateOf(Step2Focus.Name) }
    val focusManager = LocalFocusManager.current

    val attemptMoveToCharacter = {
        onValidateName()

        if (uiState.name.isNotBlank() && uiState.nameError == null) {
            focusManager.clearFocus()
            focusMode = Step2Focus.Character
        }
    }

    val nameWeight by animateFloatAsState(
        targetValue = if (focusMode == Step2Focus.Name) 5f else 1.5f,
        label = "NameWeight"
    )
    val charWeight by animateFloatAsState(
        targetValue = if (focusMode == Step2Focus.Character) 5f else 1.5f,
        label = "CharWeight"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        HeaderSection()

        Spacer(modifier = Modifier.height(12.dp))

        NameSection(
            weight = nameWeight,
            isActive = focusMode == Step2Focus.Name,
            name = uiState.name,
            nameError = uiState.nameError,
            onNameChange = onNameChange,
            onValidateName = onValidateName,
            onSectionClick = { focusMode = Step2Focus.Name },
            onNextAction = attemptMoveToCharacter
        )

        Spacer(modifier = Modifier.height(16.dp))

        CharacterSection(
            weight = charWeight,
            isActive = focusMode == Step2Focus.Character,
            selectedSex = uiState.characterSex,
            onSectionClick = attemptMoveToCharacter,
            onCharacterSelect = onCharacterSelect
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 가입 완료 버튼 ---
        RunnersHiButton(
            text = "가입 완료",
            onClick = onSignUpClick,
            style = ButtonStyle.FILLED,
            enabled = uiState.name.isNotBlank() && uiState.characterSex != null,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun HeaderSection() {
    Text(
        text = "캐릭터 생성을 위해 몇 가지 정보가 필요해요."
    )
}

@Composable
private fun ColumnScope.NameSection(
    weight: Float,
    isActive: Boolean,
    name: String,
    nameError: String?,
    onNameChange: (String) -> Unit,
    onValidateName: () -> Unit,
    onSectionClick: () -> Unit,
    onNextAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onSectionClick() }
    ) {
        Column {
            Text(
                text = "이름을 정해주세요",
                style = if (isActive) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
                color = if (isActive) Color.Black else Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isActive) {
                RunnersHiTextField(
                    title = "",
                    value = name,
                    onValueChange = onNameChange,
                    placeholder = "",
                    errorMessage = nameError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { onNextAction() }),
                    onValidate = onValidateName
                )
            } else {
                Text(
                    text = name.ifBlank { "아직 입력되지 않았습니다" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (name.isBlank()) Color.LightGray else Color.Black
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.CharacterSection(
    weight: Float,
    isActive: Boolean,
    selectedSex: Sex?,
    onSectionClick: () -> Unit,
    onCharacterSelect: (Sex) -> Unit
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onSectionClick() }
    ) {
        Column {
            Text(
                text = if (isActive) "나를 표현할\n캐릭터를 골라주세요" else "선택한 캐릭터",
                style = if (isActive) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
                color = if (isActive) Color.Black else Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isActive) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Sex.entries.forEach { sex ->
                        CharacterItem(
                            sex = sex,
                            isSelected = selectedSex == sex,
                            onClick = { onCharacterSelect(sex) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                Text(
                    text = selectedSex?.name ?: "선택되지 않음",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedSex == null) Color.LightGray else Color.Black
                )
            }
        }
    }
}

@Composable
private fun CharacterItem(
    sex: Sex,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(120.dp)
            .background(
                color = if (isSelected) RunnersHiTheme.colorScheme.primaryContainer else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) RunnersHiTheme.colorScheme.primary else Color.LightGray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // 추후 이미지 리소스로 교체 가능
        Text(
            text = sex.name,
            style = MaterialTheme.typography.titleMedium,
            color = if (isSelected) RunnersHiTheme.colorScheme.primary else Color.Gray
        )
    }
}

@Preview
@Composable
private fun Step2ContentPreview() {
    RunnersHiTheme {
        Step2Content(
            uiState = SignUpUiState(
                name = "러너스하이",
                characterSex = Sex.MALE
            ),
            onNameChange = {},
            onCharacterSelect = {},
            onSignUpClick = {},
            onValidateName = {}
        )
    }
}