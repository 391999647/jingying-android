package com.jingying.movie.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jingying.movie.data.repository.HistoryRepository
import com.jingying.movie.data.repository.MovieRepository
import com.jingying.movie.domain.model.PlayHistory
import com.jingying.movie.domain.model.Resource
import com.jingying.movie.util.AppLogger
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

    companion object {
        private const val TAG = "DetailViewModel"
    }

    private val vodId: Int = savedStateHandle["vodId"] ?: 0

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState

    init {
        loadDetail()
    }

    fun loadDetail(refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = movieRepository.getMovieDetail(vodId, refresh)) {
                is Resource.Success -> {
                    val detail = result.data
                    val history = historyRepository.getHistoryByEpisode(vodId, "")
                    _uiState.value = _uiState.value.copy(
                        movie = detail,
                        isLoading = false,
                        error = null,
                        lastHistory = history
                    )
                    AppLogger.i(TAG, "加载详情成功: ${detail.vodName}")
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                    AppLogger.e(TAG, "加载详情失败: ${result.message}")
                }
                else -> {}
            }
        }
    }

    data class DetailUiState(
        val movie: com.jingying.movie.domain.model.MovieDetail? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val lastHistory: PlayHistory? = null
    )
}