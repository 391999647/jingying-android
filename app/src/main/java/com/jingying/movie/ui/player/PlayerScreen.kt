package com.jingying.movie.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.jingying.movie.R
import com.jingying.movie.domain.model.Episode
import com.jingying.movie.domain.model.MovieDetail
import com.jingying.movie.ui.player.components.GestureOverlay
import com.jingying.movie.ui.player.components.IjkVideoPlayer
import com.jingying.movie.ui.player.components.PlayerControls
import com.jingying.movie.ui.theme.AccentRed
import com.jingying.movie.ui.theme.BackgroundWhite
import com.jingying.movie.ui.theme.BorderGray
import com.jingying.movie.ui.theme.CardBackground
import com.jingying.movie.ui.theme.PrimaryText
import com.jingying.movie.ui.theme.SecondaryText
import com.jingying.movie.ui.theme.TertiaryText
import com.jingying.movie.ui.theme.TransparentScrim
import com.jingying.movie.ui.theme.White
import com.jingying.movie.util.TimeUtil

@Composable
fun PlayerScreen(
    vodId: Int,
    initialEpisodeIndex: Int,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val view = LocalView.current
    val isLandscape = rememberIsLandscape()

    DisposableEffect(playerState.isFullscreen, isLandscape) {
        activity?.let { act ->
            val window = act.window
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (playerState.isFullscreen || isLandscape) {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        onDispose {
            activity?.let { act ->
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                val window = act.window
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveHistoryImmediate()
        }
    }

    Scaffold(
        topBar = {
            if (!playerState.isFullscreen && !isLandscape) {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.movie?.vodName ?: stringResource(id = R.string.play),
                            color = PrimaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            viewModel.saveHistoryImmediate()
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
                )
            }
        },
        containerColor = BackgroundWhite
    ) { paddingValues ->
        val movie = uiState.movie
        if (uiState.isLoading || movie == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentRed)
            }
        } else {
            if (playerState.isFullscreen || isLandscape) {
                FullscreenPlayerLayout(
                    movie = movie,
                    currentEpisodeIndex = uiState.currentEpisodeIndex,
                    playerState = playerState,
                    viewModel = viewModel,
                    onBack = {
                        viewModel.saveHistoryImmediate()
                        onBack()
                    }
                )
            } else {
                PortraitPlayerLayout(
                    movie = movie,
                    currentEpisodeIndex = uiState.currentEpisodeIndex,
                    playerState = playerState,
                    viewModel = viewModel,
                    paddingValues = paddingValues,
                    onBack = {
                        viewModel.saveHistoryImmediate()
                        onBack()
                    }
                )
            }
        }
    }
}

@Composable
private fun PortraitPlayerLayout(
    movie: MovieDetail,
    currentEpisodeIndex: Int,
    playerState: PlayerState,
    viewModel: PlayerViewModel,
    paddingValues: PaddingValues,
    onBack: () -> Unit
) {
    val episode = movie.episodes.getOrNull(currentEpisodeIndex)
    val videoUrl = episode?.url ?: ""

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                if (videoUrl.isNotBlank()) {
                    IjkVideoPlayer(
                        videoUrl = videoUrl,
                        playerState = playerState,
                        onPlayerReady = { viewModel.onPlayerReady(it) },
                        onError = { viewModel.saveHistoryImmediate() },
                        onCompletion = { viewModel.nextEpisode() },
                        modifier = Modifier.fillMaxSize()
                    )
                    GestureOverlay(
                        position = playerState.position,
                        duration = playerState.duration,
                        onSeekTo = { viewModel.seekTo(it) },
                        onToggleControls = { viewModel.toggleControls() },
                        modifier = Modifier.fillMaxSize()
                    )
                    PlayerControls(
                        title = movie.vodName,
                        isPlaying = playerState.isPlaying,
                        position = playerState.position,
                        duration = playerState.duration,
                        isFullscreen = playerState.isFullscreen,
                        visible = playerState.controlsVisible,
                        onPlayPause = { viewModel.togglePlayPause() },
                        onSeek = { ratio ->
                            viewModel.seekTo((ratio * playerState.duration).toLong())
                        },
                        onFullscreenToggle = { viewModel.setFullscreen(!playerState.isFullscreen) },
                        onBack = onBack
                    )
                } else {
                    Text(
                        text = "无播放地址",
                        color = White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
        item {
            PlayerInfoSection(
                movie = movie,
                currentEpisodeIndex = currentEpisodeIndex,
                onEpisodeClick = { viewModel.switchEpisode(it) },
                onPrevious = { viewModel.previousEpisode() },
                onNext = { viewModel.nextEpisode() }
            )
        }
    }
}

@Composable
private fun FullscreenPlayerLayout(
    movie: MovieDetail,
    currentEpisodeIndex: Int,
    playerState: PlayerState,
    viewModel: PlayerViewModel,
    onBack: () -> Unit
) {
    val episode = movie.episodes.getOrNull(currentEpisodeIndex)
    val videoUrl = episode?.url ?: ""

    Box(modifier = Modifier.fillMaxSize()) {
        if (videoUrl.isNotBlank()) {
            IjkVideoPlayer(
                videoUrl = videoUrl,
                playerState = playerState,
                onPlayerReady = { viewModel.onPlayerReady(it) },
                onError = { viewModel.saveHistoryImmediate() },
                onCompletion = { viewModel.nextEpisode() },
                modifier = Modifier.fillMaxSize()
            )
            GestureOverlay(
                position = playerState.position,
                duration = playerState.duration,
                onSeekTo = { viewModel.seekTo(it) },
                onToggleControls = { viewModel.toggleControls() },
                modifier = Modifier.fillMaxSize()
            )
            PlayerControls(
                title = movie.vodName,
                isPlaying = playerState.isPlaying,
                position = playerState.position,
                duration = playerState.duration,
                isFullscreen = true,
                visible = playerState.controlsVisible,
                onPlayPause = { viewModel.togglePlayPause() },
                onSeek = { ratio ->
                    viewModel.seekTo((ratio * playerState.duration).toLong())
                },
                onFullscreenToggle = { viewModel.setFullscreen(false) },
                onBack = onBack
            )
        }
    }
}

@Composable
private fun PlayerInfoSection(
    movie: MovieDetail,
    currentEpisodeIndex: Int,
    onEpisodeClick: (Int) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = movie.vodName,
            style = MaterialTheme.typography.titleLarge,
            color = PrimaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${movie.vodYear ?: ""} ${movie.vodArea ?: ""} ${movie.typeName}".trim(),
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onPrevious,
                enabled = currentEpisodeIndex > 0,
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = PrimaryText)
                Text(text = "上一集", color = PrimaryText)
            }
            Button(
                onClick = onNext,
                enabled = currentEpisodeIndex < movie.episodes.size - 1,
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "下一集", color = PrimaryText)
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = PrimaryText)
            }
        }
        if (movie.episodes.size > 1) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.episodes),
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(((movie.episodes.size + 3) / 4 * 48).dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(movie.episodes) { index, episode ->
                    EpisodeChip(
                        name = episode.name,
                        isSelected = index == currentEpisodeIndex,
                        onClick = { onEpisodeClick(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EpisodeChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) AccentRed else CardBackground)
            .border(
                1.dp,
                if (isSelected) AccentRed else BorderGray,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) White else PrimaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun rememberIsLandscape(): Boolean {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    return configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
}
