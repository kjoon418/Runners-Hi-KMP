package good.space.runnershi.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import good.space.runnershi.model.dto.user.QuestResponse
import good.space.runnershi.repository.QuestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val quests: List<QuestResponse> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val questRepository: QuestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun fetchQuestData() {
        if (_uiState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = questRepository.getQuestData()

            result.onSuccess { questList ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        quests = questList
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "퀘스트를 불러오는데 실패했습니다."
                    )
                }
            }
        }
    }
}
