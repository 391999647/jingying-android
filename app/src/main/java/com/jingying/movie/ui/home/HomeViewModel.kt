package com.jingying.movie.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jingying.movie.data.repository.MovieRepository
import com.jingying.movie.domain.model.Movie
import com.jingying.movie.domain.model.MovieType
import com.jingying.movie.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        // 直接从内置映射加载分类，无需调用 API
        _uiState.value = _uiState.value.copy(
            types = movieRepository.getTypes()
        )
        loadMovies()
    }

    fun loadMovies(refresh: Boolean = false) {
        viewModelScope.launch {
            val current = _uiState.value
            val page = if (refresh) 1 else current.page
            _uiState.value = current.copy(isLoading = true, error = null)

            val result = movieRepository.getMovieList(
                page = page,
                limit = 18,
                typeId = current.selectedTypeId.takeIf { it > 0 },
                forceRefresh = refresh
            )

            when (result) {
                is Resource.Success -> {
                    val (movies, pagination) = result.data
                    _uiState.value = _uiState.value.copy(
                        movies = if (page == 1) movies else current.movies + movies,
                        page = pagination.page,
                        pages = pagination.pages,
                        isLoading = false,
                        isRefreshing = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun loadMore() {
        val current = _uiState.value
        if (!current.isLoading && current.page < current.pages) {
            _uiState.value = current.copy(page = current.page + 1)
            loadMovies()
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true, page = 1)
        loadMovies(refresh = true)
    }

    fun selectType(typeId: Int) {
        _uiState.value = _uiState.value.copy(
            selectedTypeId = typeId,
            page = 1,
            movies = emptyList()
        )
        loadMovies()
    }

    data class HomeUiState(
        val types: List<MovieType> = emptyList(),
        val movies: List<Movie> = emptyList(),
        val selectedTypeId: Int = 0,
        val page: Int = 1,
        val pages: Int = 1,
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val error: String? = null
    )
}