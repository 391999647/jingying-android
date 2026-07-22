package com.jingying.movie.ui.category

import androidx.lifecycle.ViewModel
import com.jingying.movie.data.repository.MovieRepository
import com.jingying.movie.domain.model.MovieType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState

    init {
        // 直接从内置映射获取分类，无需调用 API
        val types = movieRepository.getTypes()
        _uiState.value = _uiState.value.copy(
            categories = types,
            isLoading = false
        )
    }

    data class CategoryUiState(
        val categories: List<MovieType> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )
}