package com.jingying.movie.ui.player.components

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import android.view.WindowManager
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs

@Composable
fun GestureOverlay(
    position: Long,
    duration: Long,
    onSeekTo: (Long) -> Unit,
    onToggleControls: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    var isHorizontalDragging by remember { mutableStateOf(false) }
    var isVerticalDragging by remember { mutableStateOf(false) }
    var dragStartPosition by remember { mutableLongStateOf(0L) }
    var dragStartX by remember { mutableFloatStateOf(0f) }
    var dragStartY by remember { mutableFloatStateOf(0f) }
    var currentSeekPosition by remember { mutableLongStateOf(0L) }
    var verticalValue by remember { mutableFloatStateOf(0f) }
    var gestureSide by remember { mutableStateOf(GestureSide.NONE) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onToggleControls() },
                    onDoubleTap = {}
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isHorizontalDragging = true
                        dragStartX = offset.x
                        dragStartPosition = position
                        currentSeekPosition = position
                    },
                    onDragEnd = {
                        isHorizontalDragging = false
                        onSeekTo(currentSeekPosition.coerceIn(0, duration))
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val screenWidth = size.width
                        val deltaRatio = dragAmount / screenWidth
                        val deltaMs = (deltaRatio * duration).toLong()
                        currentSeekPosition = (dragStartPosition + deltaMs).coerceIn(0, duration)
                    }
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        isVerticalDragging = true
                        dragStartY = offset.y
                        gestureSide = if (offset.x < size.width / 2f) GestureSide.LEFT else GestureSide.RIGHT
                        verticalValue = if (gestureSide == GestureSide.RIGHT) {
                            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
                        } else {
                            getBrightness(context)
                        }
                    },
                    onDragEnd = {
                        isVerticalDragging = false
                        gestureSide = GestureSide.NONE
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        val screenHeight = size.height
                        val deltaRatio = -dragAmount / screenHeight
                        if (gestureSide == GestureSide.RIGHT) {
                            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            verticalValue = (verticalValue + deltaRatio * maxVolume).coerceIn(0f, maxVolume.toFloat())
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, verticalValue.toInt(), 0)
                        } else if (gestureSide == GestureSide.LEFT) {
                            verticalValue = (verticalValue + deltaRatio).coerceIn(0.01f, 1f)
                            setBrightness(context, verticalValue)
                        }
                    }
                )
            }
    ) {
        if (isHorizontalDragging) {
            SeekIndicator(
                currentSeekPosition = currentSeekPosition,
                duration = duration,
                modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
            )
        }
        if (isVerticalDragging && gestureSide == GestureSide.RIGHT) {
            VolumeIndicator(
                currentVolume = verticalValue,
                maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat(),
                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterEnd)
            )
        }
        if (isVerticalDragging && gestureSide == GestureSide.LEFT) {
            BrightnessIndicator(
                brightness = verticalValue,
                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterStart)
            )
        }
    }
}

private enum class GestureSide { LEFT, RIGHT, NONE }

private fun getBrightness(context: Context): Float {
    return try {
        Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 255f
    } catch (e: Exception) {
        0.5f
    }
}

private fun setBrightness(context: Context, value: Float) {
    val activity = context as? Activity ?: return
    val layoutParams = activity.window.attributes
    layoutParams.screenBrightness = value
    activity.window.attributes = layoutParams
}
