package com.jingying.movie.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jingying.movie.data.repository.HistoryRepository
import com.jingying.movie.domain.model.PlayHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState

    init {
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            historyRepository.getHistoryFlow().collect { historyList ->
                _uiState.value = _uiState.value.copy(
                    history = historyList,
                    isLoading = false
                )
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            historyRepository.clearAll()
        }
    }

    data class HistoryUiState(
        val history: List<PlayHistory> = emptyList(),
        val isLoading: Boolean = true
    )
}
