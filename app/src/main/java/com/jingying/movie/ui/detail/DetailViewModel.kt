package com.jingying.movie.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jingying.movie.data.repository.HistoryRepository
import com.jingying.movie.data.repository.MovieRepository
import com.jingying.movie.domain.model.MovieDetail
import com.jingying.movie.domain.model.PlayHistory
import com.jingying.movie.domain.model.Resource
import com.jingying.movie.domain.model.ResourceSite
import com.jingying.movie.domain.model.ResourceSites
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

    fun loadDetail() {
        loadDetailWithSite(null)
    }

    fun switchResourceSite(site: ResourceSite) {
        loadDetailWithSite(site)
    }

    private fun loadDetailWithSite(resourceSite: ResourceSite?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val history = historyRepository.getHistoryByEpisode(vodId, "")
            val siteName = resourceSite?.name.takeIf { it != "默认" }
            when (val result = movieRepository.getMovieDetail(vodId, siteName)) {
                is Resource.Success -> {
                    val detail = result.data
                    val availableSites = getAvailableSites(detail)
                    _uiState.value = _uiState.value.copy(
                        movie = detail,
                        isLoading = false,
                        error = null,
                        lastHistory = history,
                        currentSite = resourceSite ?: ResourceSites.fromName(detail.resourceSite),
                        availableSites = availableSites
                    )
                    AppLogger.i(TAG, "加载详情成功: ${detail.vodName}, 资源站=${resourceSite?.name}")
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

    private fun getAvailableSites(detail: MovieDetail): List<ResourceSite> {
        val sites = mutableListOf<ResourceSite>()
        if (!detail.resourceSite.isNullOrBlank()) {
            ResourceSites.fromName(detail.resourceSite).let {
                if (it != ResourceSites.DEFAULT) sites.add(it)
            }
        }
        sites.add(ResourceSites.DEFAULT)
        ResourceSites.ALL.forEach { site ->
            if (site != ResourceSites.DEFAULT && !sites.contains(site)) {
                sites.add(site)
            }
        }
        return sites.distinctBy { it.name }
    }

    data class DetailUiState(
        val movie: MovieDetail? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val lastHistory: PlayHistory? = null,
        val currentSite: ResourceSite = ResourceSites.DEFAULT,
        val availableSites: List<ResourceSite> = emptyList()
    )
}
