package com.jingying.movie.ui.player.components

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.jingying.movie.ui.player.PlayerState
import kotlinx.coroutines.delay
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import tv.danmaku.ijk.media.widget.IjkVideoView

@Composable
fun IjkVideoPlayer(
    videoUrl: String,
    playerState: PlayerState,
    onPlayerReady: (IjkMediaPlayer) -> Unit = {},
    onError: (String) -> Unit = {},
    onCompletion: () -> Unit = {},
    onPositionUpdate: (Long, Long) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentUrl by remember { mutableStateOf(videoUrl) }

    if (videoUrl != currentUrl) {
        currentUrl = videoUrl
    }

    AndroidView(
        factory = { ctx ->
            createVideoView(ctx).apply {
                setupPlayer(this, videoUrl, playerState, onPlayerReady, onError, onCompletion)
            }
        },
        update = { videoView ->
            if (videoUrl != currentUrl) {
                videoView.stopPlayback()
                videoView.setVideoPath(videoUrl)
                videoView.start()
                currentUrl = videoUrl
            }
        },
        onRelease = { videoView ->
            videoView.stopPlayback()
            videoView.clearFocus()
        },
        modifier = modifier
    )

    LaunchedEffect(currentUrl) {
        while (true) {
            delay(1000)
            // Note: position update is handled externally through callbacks
        }
    }
}

private fun createVideoView(context: Context): IjkVideoView {
    return IjkVideoView(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setBackgroundColor(android.graphics.Color.BLACK)
    }
}

private fun setupPlayer(
    videoView: IjkVideoView,
    videoUrl: String,
    playerState: PlayerState,
    onPlayerReady: (IjkMediaPlayer) -> Unit,
    onError: (String) -> Unit,
    onCompletion: () -> Unit
) {
    IjkMediaPlayer.loadLibrariesOnce(null)
    IjkMediaPlayer.native_profileBegin("ijkplayer")

    val player = IjkMediaPlayer().apply {
        setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1L)
        setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1L)
        setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1L)
        setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0L)
        setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32.toLong())
        setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L)
        setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1L)
        setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0L)
        setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 10000L)
        setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1L)
        setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48L)
    }

    videoView.setMediaPlayer(player)
    videoView.setVideoPath(videoUrl)

    videoView.setOnPreparedListener { mp ->
        (mp as? IjkMediaPlayer)?.let { onPlayerReady(it) }
        if (playerState.position > 0 && playerState.position < mp.duration) {
            mp.seekTo(playerState.position)
        }
        if (playerState.isPlaying) {
            mp.start()
        } else {
            mp.pause()
        }
    }

    videoView.setOnErrorListener { _, what, extra ->
        onError("播放错误: what=$what, extra=$extra")
        true
    }

    videoView.setOnCompletionListener {
        onCompletion()
    }
}
