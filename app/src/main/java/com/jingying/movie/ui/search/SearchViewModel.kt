package com.jingying.movie.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jingying.movie.data.repository.MovieRepository
import com.jingying.movie.domain.model.Movie
import com.jingying.movie.domain.model.Pagination
import com.jingying.movie.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")

    init {
        _searchQuery
            .debounce(300)
            .filter { it.isNotBlank() }
            .distinctUntilChanged()
            .onEach { query ->
                search(query, refresh = true)
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                query = query,
                movies = emptyList(),
                error = null,
                isLoading = false
            )
        } else {
            _uiState.value = _uiState.value.copy(query = query)
        }
    }

    fun search(keyword: String? = null, refresh: Boolean = false) {
        viewModelScope.launch {
            val query = keyword ?: _uiState.value.query
            if (query.isBlank()) return@launch

            val current = _uiState.value
            val page = if (refresh) 1 else current.page
            _uiState.value = current.copy(isLoading = true, error = null)

            movieRepository.searchMovies(query, page, 18).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is Resource.Success -> {
                        val (movies, pagination) = result.data
                        _uiState.value = _uiState.value.copy(
                            movies = if (page == 1) movies else current.movies + movies,
                            page = pagination.page,
                            pages = pagination.pages,
                            isLoading = false,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun loadMore() {
        val current = _uiState.value
        if (!current.isLoading && current.page < current.pages) {
            _uiState.value = current.copy(page = current.page + 1)
            search(refresh = false)
        }
    }

    data class SearchUiState(
        val query: String = "",
        val movies: List<Movie> = emptyList(),
        val page: Int = 1,
        val pages: Int = 1,
        val isLoading: Boolean = false,
        val error: String? = null
    )
}
