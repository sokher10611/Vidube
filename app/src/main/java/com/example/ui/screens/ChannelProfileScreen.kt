package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CreatorEntity
import com.example.data.model.VideoEntity
import com.example.ui.components.ChannelAvatar
import com.example.ui.components.VerifiedBadge
import com.example.ui.components.VideoCard
import com.example.ui.theme.RedPrimary
import com.example.ui.util.Formatters

@Composable
fun ChannelProfileScreen(
    creator: CreatorEntity,
    videos: List<VideoEntity>,
    onBack: () -> Unit,
    onToggleSubscribe: (channelId: String, isSubscribed: Boolean) -> Unit,
    onVideoClick: (VideoEntity) -> Unit,
    onShortClick: (VideoEntity) -> Unit,
    onToggleSave: (VideoEntity) -> Unit,
    onToggleLike: (VideoEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    BackHandler { onBack() }

    val longVideos = videos.filter { !it.isShort }
    val shorts = videos.filter { it.isShort }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("channel_profile_screen")
    ) {
        // Channel Banner & Back button overlay
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                // Banner background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1E2640), Color(0xFF131722), Color(0xFF3B1E48))
                            )
                        )
                )

                // Top navigation bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                            .testTag("channel_profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Channel link copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share channel",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Channel Info Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Avatar overlapping banner
                Box(modifier = Modifier.offset(y = (-32).dp)) {
                    ChannelAvatar(
                        avatarUrl = creator.avatarUrl,
                        name = creator.name,
                        size = 72.dp,
                        modifier = Modifier.border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                    )
                }

                Column(modifier = Modifier.offset(y = (-20).dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = creator.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (creator.verified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            VerifiedBadge()
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${creator.handle} • ${Formatters.formatSubscribers(creator.subscribersCount)} • ${videos.size} videos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (creator.bio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = creator.bio,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Subscribe Action Button
                    Button(
                        onClick = { onToggleSubscribe(creator.channelId, creator.isSubscribed) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (creator.isSubscribed) MaterialTheme.colorScheme.surfaceVariant else RedPrimary,
                            contentColor = if (creator.isSubscribed) MaterialTheme.colorScheme.onSurface else Color.White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("channel_subscribe_toggle_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (creator.isSubscribed) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (creator.isSubscribed) "Subscribed" else "Subscribe",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Tabs: Videos & Shorts
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = RedPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-10).dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Videos (${longVideos.size})",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Shorts (${shorts.size})",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )
            }
        }

        // Content
        if (selectedTab == 0) {
            if (longVideos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "This channel has not posted long videos yet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(longVideos, key = { it.id }) { video ->
                    VideoCard(
                        video = video,
                        onVideoClick = onVideoClick,
                        onChannelClick = { _, _, _, _ -> },
                        onToggleSave = onToggleSave,
                        onToggleLike = onToggleLike
                    )
                }
            }
        } else {
            if (shorts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "This channel has not posted Shorts yet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(shorts, key = { it.id }) { shortVideo ->
                    VideoCard(
                        video = shortVideo,
                        onVideoClick = onShortClick,
                        onChannelClick = { _, _, _, _ -> },
                        onToggleSave = onToggleSave,
                        onToggleLike = onToggleLike
                    )
                }
            }
        }
    }
}
