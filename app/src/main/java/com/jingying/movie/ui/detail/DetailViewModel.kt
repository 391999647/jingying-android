package com.jingying.movie.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jingying.movie.data.repository.HistoryRepository
import com.jingying.movie.data.repository.MovieRepository
import com.jingying.movie.domain.model.MovieDetail
import com.jingying.movie.domain.model.PlayHistory
import com.jingying.movie.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val movieRepository: MovieRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val vodId: Int = savedStateHandle["vodId"] ?: 0

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val history = historyRepository.getHistoryByEpisode(vodId, "")
            when (val result = movieRepository.getMovieDetail(vodId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        movie = result.data,
                        isLoading = false,
                        error = null,
                        lastHistory = history
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    data class DetailUiState(
        val movie: MovieDetail? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val lastHistory: PlayHistory? = null
    )
}
