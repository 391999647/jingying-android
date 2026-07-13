package com.jingying.movie.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jingying.movie.R
import com.jingying.movie.domain.model.Episode
import com.jingying.movie.domain.model.MovieDetail
import com.jingying.movie.ui.components.EmptyState
import com.jingying.movie.ui.components.LoadingShimmer
import com.jingying.movie.ui.theme.AccentRed
import com.jingying.movie.ui.theme.BackgroundWhite
import com.jingying.movie.ui.theme.BorderGray
import com.jingying.movie.ui.theme.CardBackground
import com.jingying.movie.ui.theme.PrimaryText
import com.jingying.movie.ui.theme.SecondaryText
import com.jingying.movie.ui.theme.TertiaryText
import com.jingying.movie.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    vodId: Int,
    onBack: () -> Unit,
    onPlayClick: (Int, Int) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val movie = uiState.movie

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = movie?.vodName ?: stringResource(id = R.string.movie_info),
                        color = PrimaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        containerColor = BackgroundWhite
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                LoadingShimmer(modifier = Modifier.fillMaxSize())
            } else if (movie == null) {
                EmptyState(
                    message = uiState.error ?: stringResource(id = R.string.no_data),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item { DetailHeader(movie = movie, onPlayClick = { onPlayClick(movie.vodId, 0) }) }
                    item { SynopsisSection(content = movie.vodContent) }
                    if (movie.episodes.size > 1) {
                        item { EpisodesSection(episodes = movie.episodes, onEpisodeClick = { onPlayClick(movie.vodId, it) }) }
                    }
                    item { InfoSection(movie = movie) }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(
    movie: MovieDetail,
    onPlayClick: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Card(
                modifier = Modifier
                    .width(120.dp)
                    .aspectRatio(3f / 4f),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                AsyncImage(
                    model = movie.vodPic,
                    contentDescription = movie.vodName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.vodName,
                    style = MaterialTheme.typography.titleLarge,
                    color = PrimaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                InfoTags(movie = movie)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "导演：${movie.vodDirector ?: "未知"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "演员：${movie.vodActor ?: "未知"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(id = R.string.play))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InfoTags(movie: MovieDetail) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Tag(text = movie.typeName)
        movie.vodYear?.takeIf { it.isNotBlank() }?.let { Tag(text = it) }
        movie.vodArea?.takeIf { it.isNotBlank() }?.let { Tag(text = it) }
        movie.vodScore?.takeIf { it.isNotBlank() }?.let { Tag(text = "评分 $it", isHighlight = true) }
    }
}

@Composable
private fun Tag(text: String, isHighlight: Boolean = false) {
    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = if (isHighlight) AccentRed else BorderGray,
                shape = RoundedCornerShape(4.dp)
            )
            .background(if (isHighlight) AccentRed.copy(alpha = 0.1f) else CardBackground)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (isHighlight) AccentRed else SecondaryText
        )
    }
}

@Composable
private fun SynopsisSection(content: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = stringResource(id = R.string.synopsis),
            style = MaterialTheme.typography.titleMedium,
            color = PrimaryText
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content.trim(),
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
    }
}

@Composable
private fun EpisodesSection(
    episodes: List<Episode>,
    onEpisodeClick: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = stringResource(id = R.string.episodes),
            style = MaterialTheme.typography.titleMedium,
            color = PrimaryText
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.height(((episodes.size + 3) / 4 * 48).dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(episodes) { index, episode ->
                EpisodeChip(
                    name = episode.name,
                    onClick = { onEpisodeClick(index) }
                )
            }
        }
    }
}

@Composable
private fun EpisodeChip(name: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CardBackground)
            .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun InfoSection(movie: MovieDetail) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = stringResource(id = R.string.movie_info),
            style = MaterialTheme.typography.titleMedium,
            color = PrimaryText
        )
        Spacer(modifier = Modifier.height(8.dp))
        InfoRow(label = "类型", value = movie.typeName)
        InfoRow(label = "年份", value = movie.vodYear ?: "-")
        InfoRow(label = "地区", value = movie.vodArea ?: "-")
        InfoRow(label = "导演", value = movie.vodDirector ?: "-")
        InfoRow(label = "演员", value = movie.vodActor ?: "-")
        InfoRow(label = "更新", value = movie.vodTime ?: "-")
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "$label：",
            style = MaterialTheme.typography.bodyMedium,
            color = TertiaryText
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText
        )
    }
}
