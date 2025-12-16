package good.space.runnershi.viewmodel

import good.space.runnershi.model.dto.running.PersonalBestResponse
import good.space.runnershi.repository.RunRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MyPageUiState(
    val isLoading: Boolean = false,
    val personalBest: PersonalBestResponse? = null,
    val errorMessage: String? = null
)

class MyPageViewModel(
    private val runRepository: RunRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    
    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    init {
        fetchPersonalBest()
    }

    fun fetchPersonalBest() {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            runRepository.getPersonalBest()
                .onSuccess { record ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        personalBest = record
                    )
                }
                .onFailure { _ ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "기록을 불러오지 못했습니다."
                    )
                }
        }
    }
}

