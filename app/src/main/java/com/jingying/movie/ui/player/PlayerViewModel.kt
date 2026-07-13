package com.jingying.movie.ui.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jingying.movie.data.repository.HistoryRepository
import com.jingying.movie.data.repository.MovieRepository
import com.jingying.movie.domain.model.Episode
import com.jingying.movie.domain.model.MovieDetail
import com.jingying.movie.domain.model.PlayHistory
import com.jingying.movie.domain.model.Resource
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

    private val vodId: Int = savedStateHandle["vodId"] ?: 0
    private val initialEpisodeIndex: Int = savedStateHandle["episodeIndex"] ?: 0

    private val _uiState = MutableStateFlow(PlayerScreenUiState())
    val uiState: StateFlow<PlayerScreenUiState> = _uiState

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState

    private var saveHistoryJob: Job? = null
    private var positionTrackingJob: Job? = null
    private var mediaPlayerRef: tv.danmaku.ijk.media.player.IjkMediaPlayer? = null

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = movieRepository.getMovieDetail(vodId)) {
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
                        isPlaying = true
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

    fun onPlayerReady(mp: tv.danmaku.ijk.media.player.IjkMediaPlayer) {
        mediaPlayerRef = mp
        _playerState.value = _playerState.value.copy(
            duration = mp.duration.coerceAtLeast(0L),
            isPlaying = mp.isPlaying
        )
        startPositionTracking()
    }

    fun onPositionUpdate(position: Long, duration: Long) {
        _playerState.value = _playerState.value.copy(
            position = position,
            duration = duration
        )
    }

    fun togglePlayPause() {
        val mp = mediaPlayerRef ?: return
        if (mp.isPlaying) {
            mp.pause()
        } else {
            mp.start()
        }
        _playerState.value = _playerState.value.copy(isPlaying = mp.isPlaying)
    }

    fun seekTo(position: Long) {
        val mp = mediaPlayerRef ?: return
        val safePosition = position.coerceIn(0, mp.duration.coerceAtLeast(0L))
        mp.seekTo(safePosition)
        _playerState.value = _playerState.value.copy(position = safePosition)
    }

    fun seekBy(deltaMs: Long) {
        val mp = mediaPlayerRef ?: return
        val newPosition = (mp.currentPosition + deltaMs).coerceIn(0, mp.duration.coerceAtLeast(0L))
        mp.seekTo(newPosition)
        _playerState.value = _playerState.value.copy(position = newPosition)
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
                isPlaying = true,
                isLoading = false
            )
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

    fun saveHistoryImmediate() {
        val movie = _uiState.value.movie ?: return
        val episode = movie.episodes.getOrNull(_uiState.value.currentEpisodeIndex) ?: return
        val position = _playerState.value.position
        val duration = _playerState.value.duration
        if (duration <= 0) return

        viewModelScope.launch {
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
        }
    }

    private fun startPositionTracking() {
        positionTrackingJob?.cancel()
        positionTrackingJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val mp = mediaPlayerRef ?: continue
                val position = mp.currentPosition.coerceAtLeast(0L)
                val duration = mp.duration.coerceAtLeast(0L)
                _playerState.value = _playerState.value.copy(
                    position = position,
                    duration = duration
                )
                scheduleSaveHistory()
            }
        }
    }

    private fun scheduleSaveHistory() {
        if (saveHistoryJob?.isActive == true) return
        saveHistoryJob = viewModelScope.launch {
            delay(3000)
            saveHistoryImmediate()
        }
    }

    override fun onCleared() {
        super.onCleared()
        positionTrackingJob?.cancel()
        saveHistoryJob?.cancel()
        saveHistoryImmediate()
    }

    data class PlayerScreenUiState(
        val movie: MovieDetail? = null,
        val currentEpisodeIndex: Int = 0,
        val isLoading: Boolean = false,
        val error: String? = null
    )
}
