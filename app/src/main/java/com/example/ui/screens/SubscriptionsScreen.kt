package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CreatorEntity
import com.example.data.model.VideoEntity
import com.example.ui.components.ChannelAvatar
import com.example.ui.components.VerifiedBadge
import com.example.ui.components.VideoCard
import com.example.ui.theme.RedPrimary
import com.example.ui.util.Formatters

@Composable
fun SubscriptionsScreen(
    subscribedCreators: List<CreatorEntity>,
    allCreators: List<CreatorEntity>,
    allVideos: List<VideoEntity>,
    onVideoClick: (VideoEntity) -> Unit,
    onChannelClick: (channelId: String, name: String, avatarUrl: String, handle: String) -> Unit,
    onToggleSubscribe: (channelId: String, isSubscribed: Boolean) -> Unit,
    onToggleSave: (VideoEntity) -> Unit,
    onToggleLike: (VideoEntity) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Today", "Videos", "Shorts")

    // Filter videos by subscribed channels
    val subscribedChannelIds = subscribedCreators.map { it.channelId }.toSet()
    val subscribedVideos = allVideos.filter { it.channelId in subscribedChannelIds }

    val displayedVideos = when (selectedFilter) {
        "Videos" -> subscribedVideos.filter { !it.isShort }
        "Shorts" -> subscribedVideos.filter { it.isShort }
        else -> subscribedVideos
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("subscriptions_screen")
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Subscriptions,
                contentDescription = null,
                tint = RedPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Subscriptions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Subscribed Channels Avatar Stories Row
        if (subscribedCreators.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .testTag("subscribed_channels_row")
            ) {
                items(subscribedCreators, key = { it.channelId }) { creator ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(62.dp)
                            .clickable {
                                onChannelClick(
                                    creator.channelId,
                                    creator.name,
                                    creator.avatarUrl,
                                    creator.handle
                                )
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .border(2.dp, RedPrimary, CircleShape)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ChannelAvatar(
                                avatarUrl = creator.avatarUrl,
                                name = creator.name,
                                size = 50.dp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = creator.name,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Subscriptions Video Feed or Discover Creators Empty State
        if (subscribedCreators.isEmpty()) {
            // Discover Creators Showcase
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Never miss a video!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Subscribe to your favorite channels to see their latest videos here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item {
                    Text(
                        text = "Suggested Channels",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(allCreators, key = { it.channelId }) { creator ->
                    CreatorSuggestionCard(
                        creator = creator,
                        onChannelClick = {
                            onChannelClick(
                                creator.channelId,
                                creator.name,
                                creator.avatarUrl,
                                creator.handle
                            )
                        },
                        onToggleSubscribe = {
                            onToggleSubscribe(creator.channelId, creator.isSubscribed)
                        }
                    )
                }
            }
        } else if (displayedVideos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No videos for this filter yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("subscriptions_feed")
            ) {
                items(displayedVideos, key = { it.id }) { video ->
                    VideoCard(
                        video = video,
                        onVideoClick = onVideoClick,
                        onChannelClick = onChannelClick,
                        onToggleSave = onToggleSave,
                        onToggleLike = onToggleLike
                    )
                }
            }
        }
    }
}

@Composable
fun CreatorSuggestionCard(
    creator: CreatorEntity,
    onChannelClick: () -> Unit,
    onToggleSubscribe: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChannelClick() }
            .testTag("creator_card_${creator.channelId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChannelAvatar(
                avatarUrl = creator.avatarUrl,
                name = creator.name,
                size = 48.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = creator.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (creator.verified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerifiedBadge()
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${creator.handle} • ${Formatters.formatSubscribers(creator.subscribersCount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (creator.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = creator.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onToggleSubscribe,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (creator.isSubscribed) MaterialTheme.colorScheme.background else RedPrimary,
                    contentColor = if (creator.isSubscribed) MaterialTheme.colorScheme.onBackground else Color.White
                ),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("subscribe_button_${creator.channelId}")
            ) {
                Text(
                    text = if (creator.isSubscribed) "Subscribed" else "Subscribe",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
