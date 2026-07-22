package com.jingying.movie.ui.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jingying.movie.data.repository.HistoryRepository
import com.jingying.movie.data.repository.MovieRepository
import com.jingying.movie.domain.model.MovieDetail
import com.jingying.movie.domain.model.PlayHistory
import com.jingying.movie.domain.model.Resource
import com.jingying.movie.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val movieRepository: MovieRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    companion object {
        private const val TAG = "PlayerViewModel"
        private const val AUTO_SAVE_INTERVAL = 15_000L // 15秒自动保存一次
    }

    private val vodId: Int = savedStateHandle["vodId"] ?: 0
    private val initialEpisodeIndex: Int = savedStateHandle["episodeIndex"] ?: 0

    private val _uiState = MutableStateFlow(PlayerScreenUiState())
    val uiState: StateFlow<PlayerScreenUiState> = _uiState

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState

    private var autoSaveJob: Job? = null
    private var isDirty = false

    init {
        loadDetail()
    }

    fun loadDetail(refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = movieRepository.getMovieDetail(vodId, refresh)) {
                is Resource.Success -> {
                    val detail = result.data
                    val episodes = detail.episodes
                    val episodeIndex = initialEpisodeIndex.coerceIn(0, episodes.size.coerceAtLeast(1) - 1)
                    val episode = episodes.getOrNull(episodeIndex)
                    val history = episode?.let {
                        historyRepository.getHistoryByEpisode(vodId, it.url)
                    }
                    _uiState.value = _uiState.value.copy(
                        movie = detail,
                        currentEpisodeIndex = episodeIndex,
                        isLoading = false,
                        error = null
                    )
                    _playerState.value = _playerState.value.copy(
                        position = history?.position ?: 0L,
                        duration = 0L,
                        isPlaying = true,
                        isLoading = false
                    )
                    isDirty = false
                    startAutoSave()
                    AppLogger.i(TAG, "加载详情成功: ${detail.vodName}, 集数=$episodeIndex")
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

    fun onPositionUpdate(position: Long, duration: Long) {
        _playerState.value = _playerState.value.copy(
            position = position,
            duration = duration
        )
        isDirty = true
    }

    fun onPlaybackStateChanged(isPlaying: Boolean) {
        _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
        if (!isPlaying && isDirty) {
            saveHistoryImmediate()
        }
    }

    fun togglePlayPause() {
        _playerState.value = _playerState.value.copy(
            isPlaying = !_playerState.value.isPlaying
        )
    }

    fun seekTo(position: Long) {
        _playerState.value = _playerState.value.copy(
            seekTarget = position,
            position = position
        )
        isDirty = true
    }

    fun seekBy(deltaMs: Long) {
        val current = _playerState.value.position
        val duration = _playerState.value.duration
        val newPosition = (current + deltaMs).coerceIn(0, duration.coerceAtLeast(0))
        seekTo(newPosition)
    }

    fun switchEpisode(index: Int) {
        val movie = _uiState.value.movie ?: return
        val episodes = movie.episodes
        if (index !in episodes.indices) return

        saveHistoryImmediate()
        _uiState.value = _uiState.value.copy(
            currentEpisodeIndex = index,
            error = null
        )
        viewModelScope.launch {
            val history = historyRepository.getHistoryByEpisode(vodId, episodes[index].url)
            _playerState.value = _playerState.value.copy(
                position = history?.position ?: 0L,
                duration = 0L,
                seekTarget = history?.position ?: 0L,
                isPlaying = true,
                isLoading = false
            )
            isDirty = false
            AppLogger.i(TAG, "切换集数: ${episodes[index].name}")
        }
    }

    fun nextEpisode() {
        switchEpisode(_uiState.value.currentEpisodeIndex + 1)
    }

    fun previousEpisode() {
        switchEpisode(_uiState.value.currentEpisodeIndex - 1)
    }

    fun toggleControls() {
        _playerState.value = _playerState.value.copy(
            controlsVisible = !_playerState.value.controlsVisible
        )
    }

    fun setFullscreen(isFullscreen: Boolean) {
        _playerState.value = _playerState.value.copy(isFullscreen = isFullscreen)
    }

    fun toggleScaleType() {
        val currentType = _playerState.value.scaleType
        val nextType = when (currentType) {
            ScreenScaleType.FIT -> ScreenScaleType.FILL
            ScreenScaleType.FILL -> ScreenScaleType.ZOOM
            ScreenScaleType.ZOOM -> ScreenScaleType.FIT
        }
        _playerState.value = _playerState.value.copy(scaleType = nextType)
    }

    fun saveHistoryImmediate() {
        if (!isDirty) return
        val movie = _uiState.value.movie ?: return
        val episode = movie.episodes.getOrNull(_uiState.value.currentEpisodeIndex) ?: return
        val position = _playerState.value.position
        val duration = _playerState.value.duration
        if (duration <= 0) return

        viewModelScope.launch {
            try {
                historyRepository.saveHistory(
                    PlayHistory(
                        vodId = movie.vodId,
                        vodName = movie.vodName,
                        vodPic = movie.vodPic,
                        episodeName = episode.name,
                        episodeUrl = episode.url,
                        position = position,
                        duration = duration
                    )
                )
                isDirty = false
                AppLogger.d(TAG, "保存进度: ${position}ms / ${duration}ms")
            } catch (e: Exception) {
                AppLogger.e(TAG, "保存进度失败", e)
            }
        }
    }

    private fun startAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            while (true) {
                delay(AUTO_SAVE_INTERVAL)
                if (isDirty) {
                    saveHistoryImmediate()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
        saveHistoryImmediate()
    }

    data class PlayerScreenUiState(
        val movie: MovieDetail? = null,
        val currentEpisodeIndex: Int = 0,
        val isLoading: Boolean = false,
        val error: String? = null
    )
}