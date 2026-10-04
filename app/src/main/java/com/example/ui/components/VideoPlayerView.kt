package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoEntity
import com.example.ui.theme.RedPrimary
import com.example.ui.util.Formatters
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerView(
    video: VideoEntity,
    modifier: Modifier = Modifier,
    onFullScreenToggle: (() -> Unit)? = null
) {
    var isPlaying by remember { mutableStateOf(true) }
    var currentSeconds by remember(video.id) { mutableIntStateOf(0) }
    var showControls by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var showSpeedMenu by remember { mutableStateOf(false) }

    val totalDuration = video.durationSeconds.coerceAtLeast(1)

    // Auto-advance playback timer while playing
    LaunchedEffect(isPlaying, playbackSpeed, video.id) {
        while (isPlaying) {
            val delayMillis = (1000L / playbackSpeed).toLong().coerceAtLeast(200L)
            delay(delayMillis)
            if (currentSeconds < totalDuration) {
                currentSeconds += 1
            } else {
                isPlaying = false
                currentSeconds = 0
            }
        }
    }

    // Auto hide controls after 4 seconds
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(4000)
            showControls = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
            .testTag("video_player_container")
    ) {
        // Video playback background / canvas simulation
        VideoCanvasVisualizer(
            isPlaying = isPlaying,
            category = video.category,
            currentSeconds = currentSeconds,
            totalDuration = totalDuration
        )

        // Overlay Controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(8.dp)
            ) {
                // Top controls bar: Quality + Speed selector + Mute
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "4K • 60 FPS",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Mute button
                        IconButton(
                            onClick = { isMuted = !isMuted },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("player_mute_toggle")
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                contentDescription = if (isMuted) "Unmute" else "Mute",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Playback speed menu button
                        Box {
                            IconButton(
                                onClick = { showSpeedMenu = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("player_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Playback settings",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showSpeedMenu,
                                onDismissRequest = { showSpeedMenu = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                Text(
                                    text = "Playback Speed",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                                listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = if (speed == 1.0f) "Normal (1.0x)" else "${speed}x",
                                                    color = if (playbackSpeed == speed) RedPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (playbackSpeed == speed) {
                                                    Text("✓", color = RedPrimary, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        },
                                        onClick = {
                                            playbackSpeed = speed
                                            showSpeedMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Middle Play / Pause / Seek 10s controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind 10s
                    IconButton(
                        onClick = { currentSeconds = (currentSeconds - 10).coerceAtLeast(0) },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                            .testTag("player_rewind_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Rewind 10 seconds",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Main Play/Pause
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(RedPrimary.copy(alpha = 0.9f))
                            .testTag("player_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Forward 10s
                    IconButton(
                        onClick = { currentSeconds = (currentSeconds + 10).coerceAtMost(totalDuration) },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                            .testTag("player_forward_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Fast forward 10 seconds",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Bottom Timeline Scrubber & Duration
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${Formatters.formatDuration(currentSeconds)} / ${Formatters.formatDuration(totalDuration)}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (playbackSpeed != 1.0f) {
                                Text(
                                    text = "${playbackSpeed}x",
                                    color = RedPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }
                            IconButton(
                                onClick = { onFullScreenToggle?.invoke() },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Slider(
                        value = currentSeconds.toFloat(),
                        onValueChange = { newValue ->
                            currentSeconds = newValue.toInt()
                        },
                        valueRange = 0f..totalDuration.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = RedPrimary,
                            activeTrackColor = RedPrimary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .testTag("video_timeline_slider")
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoCanvasVisualizer(
    isPlaying: Boolean,
    category: String,
    currentSeconds: Int,
    totalDuration: Int
) {
    val baseColors = when (category.lowercase()) {
        "tech" -> listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
        "gaming" -> listOf(Color(0xFF2C061F), Color(0xFF374045), Color(0xFFE040FB))
        "music" -> listOf(Color(0xFF0F0C29), Color(0xFF302B63), Color(0xFF24243E))
        "travel" -> listOf(Color(0xFF134E5E), Color(0xFF71B280), Color(0xFF267871))
        "food" -> listOf(Color(0xFF3E2723), Color(0xFFBF360C), Color(0xFFFF6F00))
        else -> listOf(Color(0xFF141E30), Color(0xFF243B55), Color(0xFF00ADB5))
    }

    // Dynamic animated motion shift based on playback
    val progress = (currentSeconds.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = baseColors,
                    start = androidx.compose.ui.geometry.Offset(0f, progress * 400f),
                    end = androidx.compose.ui.geometry.Offset(800f, 600f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Glowing radial center
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            (if (isPlaying) RedPrimary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.1f)),
                            Color.Transparent
                        ),
                        radius = 450f
                    )
                )
        )

        // Waveform audio/visualizer bars
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(16) { index ->
                val waveHeight = if (isPlaying) {
                    val multiplier = ((index * 7 + currentSeconds * 9) % 35) + 12
                    multiplier.dp
                } else {
                    8.dp
                }
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(waveHeight)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.35f + (index % 4) * 0.15f))
                )
            }
        }
    }
}
