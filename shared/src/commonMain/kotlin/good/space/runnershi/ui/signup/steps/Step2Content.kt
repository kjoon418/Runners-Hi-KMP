package good.space.runnershi.ui.signup.steps

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import good.space.runnershi.model.domain.auth.Sex
import good.space.runnershi.ui.character.CharacterAppearance
import good.space.runnershi.ui.character.ItemType
import good.space.runnershi.ui.character.LayeredCharacter
import good.space.runnershi.ui.character.defaultResources
import good.space.runnershi.ui.components.ButtonStyle
import good.space.runnershi.ui.components.RunnersHiButton
import good.space.runnershi.ui.components.RunnersHiTextField
import good.space.runnershi.ui.signup.SignUpUiState
import good.space.runnershi.ui.theme.RunnersHiTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

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
    val scrollState = rememberScrollState()

    val attemptMoveToCharacter = {
        onValidateName()

        if (uiState.name.isNotBlank()
            && uiState.nameError == null
            && uiState.nameVerified
        ) {
            focusManager.clearFocus()
            focusMode = Step2Focus.Character
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
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
            isActive = focusMode == Step2Focus.Character,
            selectedSex = uiState.characterSex,
            onSectionClick = attemptMoveToCharacter,
            onCharacterSelect = onCharacterSelect
        )

        Spacer(modifier = Modifier.height(16.dp))

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
        text = "캐릭터 생성을 위해\n몇 가지 정보가 필요해요",
        style = RunnersHiTheme.typography.titleLarge,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun NameSection(
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
            .fillMaxWidth()
            .animateContentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onSectionClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    text = name.ifBlank { "" },
                    style = MaterialTheme.typography.titleLarge,
                    color = if (name.isBlank()) Color.LightGray else Color.Black
                )
            }
        }
    }
}

@Composable
private fun CharacterSection(
    isActive: Boolean,
    selectedSex: Sex?,
    onSectionClick: () -> Unit,
    onCharacterSelect: (Sex) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onSectionClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isActive) "나를 표현할\n캐릭터를 골라주세요" else "선택한 캐릭터",
                style = if (isActive) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                color = if (isActive) Color.Black else Color.Gray,
                textAlign = TextAlign.Center
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Sex.entries.forEach { sex ->
                        Box(modifier = Modifier.alpha(0.6f)) {
                            LayeredCharacter(
                                appearance = getCharacterAppearance(sex),
                                modifier = Modifier.size(60.dp),
                                isPlaying = false
                            )
                        }
                    }
                }
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
            .height(180.dp)
            .background(
                color = if (isSelected) RunnersHiTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent,
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LayeredCharacter(
                appearance = getCharacterAppearance(sex),
                modifier = Modifier
                    .size(120.dp)
                    .padding(0.dp),
                isPlaying = isSelected
            )
        }
    }
}

private fun getCharacterAppearance(sex: Sex): CharacterAppearance {
    return CharacterAppearance(
        base = defaultResources(sex, ItemType.BASE) ?: emptyList(),

        hair = defaultResources(sex, ItemType.HAIR),
        top = defaultResources(sex, ItemType.TOP),
        bottom = defaultResources(sex, ItemType.BOTTOM),
        shoes = defaultResources(sex, ItemType.SHOES),
        head = defaultResources(sex, ItemType.HEAD),
    )
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
