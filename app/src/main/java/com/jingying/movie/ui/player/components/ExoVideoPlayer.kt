package com.jingying.movie.ui.player.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.DefaultHlsDataSourceFactory
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.jingying.movie.ui.player.PlayerState

@Composable
fun ExoVideoPlayer(
    videoUrl: String,
    playerState: PlayerState,
    onPositionUpdate: (Long, Long) -> Unit = { _, _ -> },
    onPlaybackStateChanged: (Boolean) -> Unit = {},
    onError: (String) -> Unit = {},
    onCompletion: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            .setDefaultRequestProperties(mapOf(
                "Referer" to "https://uuuu.111u.icu/"
            ))

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                15_000,
                50_000,
                2_500,
                5_000
            ).build()

        val hlsDataSourceFactory = DefaultHlsDataSourceFactory { httpDataSourceFactory }
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)
            .setHlsDataSourceFactory(hlsDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .build().apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            onPositionUpdate(currentPosition, duration)
                        }
                        if (playbackState == Player.STATE_ENDED) {
                            onCompletion()
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        onPlaybackStateChanged(isPlaying)
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        onError("播放错误: ${error.message}")
                    }
                })
            }
    }

    // 定时同步位置到 ViewModel (每秒)
    LaunchedEffect(exoPlayer, playerState.isPlaying) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            if (exoPlayer.duration > 0) {
                onPositionUpdate(exoPlayer.currentPosition, exoPlayer.duration)
            }
        }
    }

    // ViewModel 状态 -> ExoPlayer 控制
    LaunchedEffect(playerState.isPlaying) {
        exoPlayer.playWhenReady = playerState.isPlaying
    }

    LaunchedEffect(playerState.seekTarget) {
        val target = playerState.seekTarget
        if (target >= 0 && target != exoPlayer.currentPosition) {
            exoPlayer.seekTo(target.coerceIn(0, exoPlayer.duration.coerceAtLeast(0)))
        }
    }

    DisposableEffect(videoUrl) {
        val mediaItem = MediaItem.fromUri(videoUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = playerState.isPlaying

        if (playerState.position > 0) {
            exoPlayer.seekTo(playerState.position)
        }

        onDispose {}
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = false
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = modifier
    )
}
