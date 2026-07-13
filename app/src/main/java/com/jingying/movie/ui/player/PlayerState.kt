package com.jingying.movie.ui.player

data class PlayerState(
    val isPlaying: Boolean = true,
    val position: Long = 0L,
    val duration: Long = 0L,
    val seekTarget: Long = -1L,
    val isLoading: Boolean = false,
    val isFullscreen: Boolean = false,
    val controlsVisible: Boolean = true
)
