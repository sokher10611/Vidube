package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbDownOffAlt
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CreatorEntity
import com.example.data.model.VideoEntity
import com.example.ui.components.ChannelAvatar
import com.example.ui.theme.RedPrimary
import com.example.ui.util.Formatters
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ShortsScreen(
    shorts: List<VideoEntity>,
    creators: List<CreatorEntity>,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit,
    onToggleLike: (VideoEntity) -> Unit,
    onToggleDislike: (VideoEntity) -> Unit,
    onOpenComments: (VideoEntity) -> Unit,
    onToggleSubscribe: (channelId: String, isSubscribed: Boolean) -> Unit,
    onChannelClick: (channelId: String, name: String, avatarUrl: String, handle: String) -> Unit,
    onUploadClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (shorts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No Shorts available yet",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onUploadClick,
                    colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload First Short")
                }
            }
        }
        return
    }

    val safeIndex = currentIndex.coerceIn(0, shorts.size - 1)
    val activeShort = shorts[safeIndex]
    val creator = creators.firstOrNull { it.channelId == activeShort.channelId }
    val isSubscribed = creator?.isSubscribed ?: false

    // Double tap heart burst state
    var showHeartBurst by remember { mutableStateOf(false) }

    // Vinyl rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_angle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("shorts_screen_container")
            .pointerInput(activeShort.id) {
                detectTapGestures(
                    onDoubleTap = {
                        if (!activeShort.isLiked) {
                            onToggleLike(activeShort)
                        }
                        showHeartBurst = true
                    }
                )
            }
    ) {
        // Short Canvas / Background Animation
        ShortCanvasBackground(category = activeShort.category)

        // Top Shorts Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Shorts",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )

            // Switch short arrows (up / down)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        if (safeIndex > 0) onIndexChange(safeIndex - 1)
                    },
                    enabled = safeIndex > 0,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .testTag("shorts_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandLess,
                        contentDescription = "Previous short",
                        tint = if (safeIndex > 0) Color.White else Color.Gray
                    )
                }

                Text(
                    text = "${safeIndex + 1}/${shorts.size}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        if (safeIndex < shorts.size - 1) onIndexChange(safeIndex + 1)
                    },
                    enabled = safeIndex < shorts.size - 1,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .testTag("shorts_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Next short",
                        tint = if (safeIndex < shorts.size - 1) Color.White else Color.Gray
                    )
                }
            }
        }

        // Heart Burst Animation on Double Tap
        if (showHeartBurst) {
            LaunchedEffect(showHeartBurst) {
                delay(800)
                showHeartBurst = false
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = RedPrimary.copy(alpha = 0.9f),
                    modifier = Modifier.size(110.dp)
                )
            }
        }

        // Right Action Bar (Like, Dislike, Comments, Share, Disc)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onToggleLike(activeShort) },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .testTag("short_like_button_${activeShort.id}")
                ) {
                    Icon(
                        imageVector = if (activeShort.isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Like",
                        tint = if (activeShort.isLiked) RedPrimary else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = Formatters.formatViews(activeShort.likes),
                    color = if (activeShort.isLiked) RedPrimary else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Dislike Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onToggleDislike(activeShort) },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .testTag("short_dislike_button_${activeShort.id}")
                ) {
                    Icon(
                        imageVector = if (activeShort.isDisliked) Icons.Default.ThumbDown else Icons.Default.ThumbDownOffAlt,
                        contentDescription = "Dislike",
                        tint = if (activeShort.isDisliked) RedPrimary else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Dislike",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }

            // Comments Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { onOpenComments(activeShort) },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .testTag("short_comments_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comments",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Comment",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Share Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .testTag("short_share_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Share",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }

            // Rotating Vinyl Sound Disc
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222222))
                    .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    .rotate(rotation),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(RedPrimary)
                )
            }
        }

        // Bottom Left Info Overlay: Channel info + Subscribe + Title + Music
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(start = 16.dp, bottom = 48.dp)
        ) {
            // Channel row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChannelAvatar(
                    avatarUrl = activeShort.channelAvatarUrl,
                    name = activeShort.channelName,
                    size = 36.dp,
                    modifier = Modifier.clickable {
                        onChannelClick(
                            activeShort.channelId,
                            activeShort.channelName,
                            activeShort.channelAvatarUrl,
                            activeShort.channelHandle
                        )
                    }
                )

                Text(
                    text = activeShort.channelHandle.ifBlank { activeShort.channelName },
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Subscribe Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isSubscribed) Color.White.copy(alpha = 0.25f) else RedPrimary)
                        .clickable { onToggleSubscribe(activeShort.channelId, isSubscribed) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("short_subscribe_button_${activeShort.channelId}")
                ) {
                    Text(
                        text = if (isSubscribed) "Subscribed" else "Subscribe",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Caption / Title
            Text(
                text = activeShort.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Audio track ticker
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Original Audio - ${activeShort.channelName}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ShortCanvasBackground(category: String) {
    val gradientColors = when (category.lowercase()) {
        "tech" -> listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF0F0C29))
        "gaming" -> listOf(Color(0xFF2C061F), Color(0xFF431C5D), Color(0xFF0F0C29))
        "music" -> listOf(Color(0xFF16222F), Color(0xFF3B1E48), Color(0xFF0C1017))
        "food" -> listOf(Color(0xFF331608), Color(0xFF4A1F10), Color(0xFF1A0A04))
        else -> listOf(Color(0xFF141E30), Color(0xFF243B55), Color(0xFF0B0E14))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = gradientColors
                )
            )
    ) {
        // Visual glowing orb at center
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(RedPrimary.copy(alpha = 0.15f), Color.Transparent),
                        radius = 800f
                    )
                )
        )
    }
}
