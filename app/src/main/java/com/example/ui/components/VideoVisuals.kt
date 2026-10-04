package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.RedPrimary
import com.example.ui.util.Formatters

@Composable
fun VideoThumbnailBox(
    thumbnailUrl: String,
    category: String,
    durationSeconds: Int,
    isShort: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gradientBrush = when (category.lowercase()) {
        "tech" -> Brush.linearGradient(
            colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
        )
        "gaming" -> Brush.linearGradient(
            colors = listOf(Color(0xFF3A1C71), Color(0xFFD76D77), Color(0xFFFF8B7D))
        )
        "music" -> Brush.linearGradient(
            colors = listOf(Color(0xFF141E30), Color(0xFF243B55), Color(0xFF8A2387))
        )
        "travel" -> Brush.linearGradient(
            colors = listOf(Color(0xFF134E5E), Color(0xFF71B280), Color(0xFF0F4C81))
        )
        "food" -> Brush.linearGradient(
            colors = listOf(Color(0xFF42275A), Color(0xFF734B6D), Color(0xFFF37335))
        )
        else -> Brush.linearGradient(
            colors = listOf(Color(0xFF1F1C2C), Color(0xFF928DAB), Color(0xFF2C3E50))
        )
    }

    Box(
        modifier = modifier
            .background(gradientBrush)
    ) {
        // If image URL is remote or local drawable
        if (thumbnailUrl.startsWith("http") || thumbnailUrl.startsWith("android.resource")) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = "Video thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Dynamic styled backdrop with glowing badge
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                            radius = 600f
                        )
                    )
            )
            // Centered category artistic icon & title
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Category Tag (Top-left)
        if (category.isNotBlank() && !isShort) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = category.uppercase(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Duration Badge (Bottom-right)
        Box(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.BottomEnd)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.82f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (isShort) "SHORT • ${Formatters.formatDuration(durationSeconds)}" else Formatters.formatDuration(durationSeconds),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ChannelAvatar(
    avatarUrl: String,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl.isNotBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "$name avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "V"
            Text(
                text = initial,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.45f).sp
            )
        }
    }
}

@Composable
fun VerifiedBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(Color(0xFF888888)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Verified channel",
            tint = Color.Black,
            modifier = Modifier.size(10.dp)
        )
    }
}
