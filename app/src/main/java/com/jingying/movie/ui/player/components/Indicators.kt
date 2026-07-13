package com.jingying.movie.ui.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FastForward
import androidx.compose.material.icons.automirrored.filled.FastRewind
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jingying.movie.ui.theme.TransparentScrim
import com.jingying.movie.ui.theme.White
import com.jingying.movie.util.TimeUtil

@Composable
fun SeekIndicator(
    currentSeekPosition: Long,
    duration: Long,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(TransparentScrim)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (currentSeekPosition >= 0) Icons.AutoMirrored.Filled.FastForward else Icons.AutoMirrored.Filled.FastRewind,
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "${TimeUtil.formatDuration(currentSeekPosition)} / ${TimeUtil.formatDuration(duration)}",
                color = White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun VolumeIndicator(
    currentVolume: Float,
    maxVolume: Float,
    modifier: Modifier = Modifier
) {
    val ratio = if (maxVolume > 0) currentVolume / maxVolume else 0f
    IndicatorBox(
        icon = { Icon(Icons.Default.VolumeUp, null, tint = White, modifier = Modifier.size(28.dp)) },
        text = "${(ratio * 100).toInt()}%",
        modifier = modifier
    )
}

@Composable
fun BrightnessIndicator(
    brightness: Float,
    modifier: Modifier = Modifier
) {
    IndicatorBox(
        icon = { Icon(Icons.Default.BrightnessHigh, null, tint = White, modifier = Modifier.size(28.dp)) },
        text = "${(brightness * 100).toInt()}%",
        modifier = modifier
    )
}

@Composable
private fun IndicatorBox(
    icon: @Composable () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(24.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(TransparentScrim)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            icon()
            Text(text = text, color = White, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
