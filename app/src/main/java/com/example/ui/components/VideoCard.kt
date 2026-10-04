package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoEntity
import com.example.ui.theme.RedPrimary
import com.example.ui.util.Formatters

@Composable
fun VideoCard(
    video: VideoEntity,
    onVideoClick: (VideoEntity) -> Unit,
    onChannelClick: (channelId: String, name: String, avatarUrl: String, handle: String) -> Unit,
    onToggleSave: (VideoEntity) -> Unit,
    onToggleLike: (VideoEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onVideoClick(video) }
            .testTag("video_card_${video.id}")
            .padding(bottom = 16.dp)
    ) {
        // Thumbnail Box
        VideoThumbnailBox(
            thumbnailUrl = video.thumbnailUrl,
            category = video.category,
            durationSeconds = video.durationSeconds,
            isShort = video.isShort,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Info Row: Avatar + Title & Meta + Options Menu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Channel Avatar
            ChannelAvatar(
                avatarUrl = video.channelAvatarUrl,
                name = video.channelName,
                size = 38.dp,
                modifier = Modifier
                    .clickable {
                        onChannelClick(
                            video.channelId,
                            video.channelName,
                            video.channelAvatarUrl,
                            video.channelHandle
                        )
                    }
                    .testTag("video_card_channel_avatar_${video.id}")
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = video.channelName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable {
                            onChannelClick(
                                video.channelId,
                                video.channelName,
                                video.channelAvatarUrl,
                                video.channelHandle
                            )
                        }
                    )

                    VerifiedBadge()

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${Formatters.formatViews(video.views)} views",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = Formatters.formatTimeAgo(video.uploadTimestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // More Options
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("video_card_more_menu_${video.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(if (video.isSaved) "Remove from Watch Later" else "Save to Watch Later")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (video.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = if (video.isSaved) RedPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onToggleSave(video)
                            showMenu = false
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(if (video.isLiked) "Liked" else "Like video")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (video.isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = null,
                                tint = if (video.isLiked) RedPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onToggleLike(video)
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}
