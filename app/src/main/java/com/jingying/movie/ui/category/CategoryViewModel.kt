package com.jingying.movie.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jingying.movie.data.repository.MovieRepository
import com.jingying.movie.domain.model.MovieType
import com.jingying.movie.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = movieRepository.getTypes()) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        categories = result.data,
                        isLoading = false
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
                else -> {}
            }
        }
    }

    data class CategoryUiState(
        val categories: List<MovieType> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )
}
