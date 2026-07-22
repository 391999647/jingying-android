package com.jingying.movie.ui.player.components

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import com.jingying.movie.ui.player.PlayerState
import com.jingying.movie.util.VideoCacheManager
import kotlinx.coroutines.delay

/**
 * 优化后的视频播放器组件
 *
 * 功能：
 * - 接入 VideoCacheManager 磁盘缓存，避免重复下载分片
 * - 根据网络类型（移动数据/Wi-Fi）动态限制分辨率与码率
 * - 根据网络类型动态缩减预缓冲时长
 * - 绑定 Lifecycle，后台自动暂停以节省流量
 * - 通过 onPositionUpdate / onPlaybackStateChanged 回调实时反馈播放状态
 */
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
    val lifecycleOwner = LocalLifecycleOwner.current

    // 判断当前网络类型
    val isMetered = remember { isActiveNetworkMetered(context) }

    // 根据网络类型选择参数
    val (maxVideoHeight, maxBitrate, minBufferMs, maxBufferMs) = remember {
        if (isMetered) {
            // 移动数据网络：严格限制
            PlayerLimits(720, 1_500_000, 5_000, 15_000)
        } else {
            // Wi-Fi 网络：适度限制（防止跑 4K）
            PlayerLimits(1080, 4_000_000, 10_000, 30_000)
        }
    }

    val exoPlayer = remember {
        // 1. 数据源工厂：HttpDataSource 包裹 CacheDataSource
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            .setDefaultRequestProperties(mapOf(
                "Referer" to "https://uuuu.111u.icu/"
            ))
            .setAllowCrossProtocolRedirects(true)

        val cache = VideoCacheManager.getCache(context)
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(FLAG_IGNORE_CACHE_ON_ERROR)

        // 2. TrackSelector：限制分辨率与码率
        val trackSelector = DefaultTrackSelector(context).apply {
            setParameters(buildUponParameters()
                .setMaxVideoSize(Int.MAX_VALUE, maxVideoHeight)
                .setMaxVideoBitrate(maxBitrate)
            )
        }

        // 3. LoadControl：缩减预缓冲
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                minBufferMs,
                maxBufferMs,
                2_500,
                5_000
            ).build()

        // 4. 构建播放器
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .setLoadControl(loadControl)
            .setTrackSelector(trackSelector)
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

    // 5. 生命周期绑定：后台暂停
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP -> {
                    if (exoPlayer.playWhenReady) {
                        exoPlayer.playWhenReady = false
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 定时同步位置到 ViewModel（每秒）
    LaunchedEffect(exoPlayer, playerState.isPlaying) {
        while (true) {
            delay(1000)
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

        onDispose {
            exoPlayer.stop()
        }
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
        update = { playerView ->
            playerView.resizeMode = playerState.scaleType.value
        },
        modifier = modifier
    )
}

/** 播放器限制参数（替代 Triple，支持 4 个字段） */
private data class PlayerLimits(
    val maxVideoHeight: Int,
    val maxBitrate: Int,
    val minBufferMs: Int,
    val maxBufferMs: Int
)

/** 判断当前活动网络是否为计费网络（移动数据） */
private fun isActiveNetworkMetered(context: android.content.Context): Boolean {
    val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
}