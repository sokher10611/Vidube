package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.VideoEntity
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.UploadVideoDialog
import com.example.ui.screens.ChannelProfileScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ShortsScreen
import com.example.ui.screens.SubscriptionsScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.screens.VideoDetailScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RedPrimary
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.VidWaveViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: VidWaveViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VidWaveApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun VidWaveApp(viewModel: VidWaveViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeVideo by viewModel.activeVideo.collectAsStateWithLifecycle()
    val activeCreator by viewModel.activeCreator.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val homeVideos by viewModel.homeVideos.collectAsStateWithLifecycle()
    val shorts by viewModel.shorts.collectAsStateWithLifecycle()
    val allCreators by viewModel.allCreators.collectAsStateWithLifecycle()
    val subscribedCreators by viewModel.subscribedCreators.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val likedVideos by viewModel.likedVideos.collectAsStateWithLifecycle()
    val savedVideos by viewModel.savedVideos.collectAsStateWithLifecycle()
    val userUploadedVideos by viewModel.userUploadedVideos.collectAsStateWithLifecycle()
    val activeVideoComments by viewModel.activeVideoComments.collectAsStateWithLifecycle()
    val activeCreatorVideos by viewModel.activeCreatorVideos.collectAsStateWithLifecycle()

    val isUploadDialogOpen by viewModel.isUploadDialogOpen.collectAsStateWithLifecycle()
    val isCommentsSheetOpen by viewModel.isCommentsSheetOpen.collectAsStateWithLifecycle()
    val isEditProfileOpen by viewModel.isEditProfileOpen.collectAsStateWithLifecycle()
    val currentShortIndex by viewModel.currentShortIndex.collectAsStateWithLifecycle()

    // Handle back button for sub-screens
    BackHandler(enabled = activeVideo != null || activeCreator != null || currentTab != MainTab.HOME) {
        when {
            activeVideo != null -> viewModel.closeVideo()
            activeCreator != null -> viewModel.closeCreatorProfile()
            currentTab != MainTab.HOME -> viewModel.selectTab(MainTab.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Show bottom navigation bar when not on full video detail player
            if (activeVideo == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("main_bottom_nav_bar")
                ) {
                    // 1. Home
                    NavigationBarItem(
                        selected = currentTab == MainTab.HOME && activeCreator == null,
                        onClick = {
                            viewModel.closeCreatorProfile()
                            viewModel.selectTab(MainTab.HOME)
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.HOME && activeCreator == null) Icons.Default.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RedPrimary,
                            selectedTextColor = RedPrimary,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    // 2. Shorts
                    NavigationBarItem(
                        selected = currentTab == MainTab.SHORTS && activeCreator == null,
                        onClick = {
                            viewModel.closeCreatorProfile()
                            viewModel.selectTab(MainTab.SHORTS)
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.SHORTS && activeCreator == null) Icons.Default.ElectricBolt else Icons.Outlined.ElectricBolt,
                                contentDescription = "Shorts"
                            )
                        },
                        label = { Text("Shorts", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RedPrimary,
                            selectedTextColor = RedPrimary,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_shorts")
                    )

                    // 3. Create (+) Action
                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.setUploadDialogOpen(true) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Upload",
                                tint = RedPrimary,
                                modifier = Modifier.size(34.dp)
                            )
                        },
                        label = { Text("Upload", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("nav_item_upload")
                    )

                    // 4. Subscriptions
                    NavigationBarItem(
                        selected = currentTab == MainTab.SUBSCRIPTIONS && activeCreator == null,
                        onClick = {
                            viewModel.closeCreatorProfile()
                            viewModel.selectTab(MainTab.SUBSCRIPTIONS)
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.SUBSCRIPTIONS && activeCreator == null) Icons.Default.Subscriptions else Icons.Outlined.Subscriptions,
                                contentDescription = "Subscriptions"
                            )
                        },
                        label = { Text("Subscriptions", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RedPrimary,
                            selectedTextColor = RedPrimary,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_subscriptions")
                    )

                    // 5. You (Profile)
                    NavigationBarItem(
                        selected = currentTab == MainTab.YOU && activeCreator == null,
                        onClick = {
                            viewModel.closeCreatorProfile()
                            viewModel.selectTab(MainTab.YOU)
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.YOU && activeCreator == null) Icons.Default.AccountCircle else Icons.Outlined.AccountCircle,
                                contentDescription = "You"
                            )
                        },
                        label = { Text("You", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RedPrimary,
                            selectedTextColor = RedPrimary,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_you")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                // If viewing a full long-form video
                activeVideo != null -> {
                    val currentVideo = activeVideo!!
                    val videoCreator = allCreators.firstOrNull { it.channelId == currentVideo.channelId }
                    VideoDetailScreen(
                        video = currentVideo,
                        creator = videoCreator,
                        comments = activeVideoComments,
                        recommendedVideos = homeVideos,
                        onBack = { viewModel.closeVideo() },
                        onToggleLike = { viewModel.toggleLike(it) },
                        onToggleDislike = { viewModel.toggleDislike(it) },
                        onToggleSave = { viewModel.toggleSave(it) },
                        onToggleSubscribe = { channelId, isSub ->
                            viewModel.toggleSubscription(channelId, isSub)
                        },
                        onChannelClick = { id, name, avatar, handle ->
                            viewModel.closeVideo()
                            viewModel.openCreatorProfileById(id, name, avatar, handle)
                        },
                        onVideoClick = { viewModel.openVideo(it) },
                        onOpenComments = { viewModel.setCommentsSheetOpen(true) }
                    )
                }

                // If viewing a Creator channel profile
                activeCreator != null -> {
                    ChannelProfileScreen(
                        creator = activeCreator!!,
                        videos = activeCreatorVideos,
                        onBack = { viewModel.closeCreatorProfile() },
                        onToggleSubscribe = { channelId, isSub ->
                            viewModel.toggleSubscription(channelId, isSub)
                        },
                        onVideoClick = { viewModel.openVideo(it) },
                        onShortClick = {
                            viewModel.closeCreatorProfile()
                            viewModel.selectTab(MainTab.SHORTS)
                        },
                        onToggleSave = { viewModel.toggleSave(it) },
                        onToggleLike = { viewModel.toggleLike(it) }
                    )
                }

                // Primary Bottom Tab Destinations
                else -> {
                    when (currentTab) {
                        MainTab.HOME -> {
                            HomeScreen(
                                videos = homeVideos,
                                shorts = shorts,
                                userProfile = userProfile,
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategory,
                                onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                                onCategorySelect = { viewModel.onCategorySelect(it) },
                                onVideoClick = { viewModel.openVideo(it) },
                                onShortClick = {
                                    val index = shorts.indexOfFirst { s -> s.id == it.id }
                                    if (index >= 0) viewModel.setShortIndex(index)
                                    viewModel.selectTab(MainTab.SHORTS)
                                },
                                onChannelClick = { id, name, avatar, handle ->
                                    viewModel.openCreatorProfileById(id, name, avatar, handle)
                                },
                                onToggleSave = { viewModel.toggleSave(it) },
                                onToggleLike = { viewModel.toggleLike(it) },
                                onProfileClick = { viewModel.selectTab(MainTab.YOU) }
                            )
                        }

                        MainTab.SHORTS -> {
                            ShortsScreen(
                                shorts = shorts,
                                creators = allCreators,
                                currentIndex = currentShortIndex,
                                onIndexChange = { viewModel.setShortIndex(it) },
                                onToggleLike = { viewModel.toggleLike(it) },
                                onToggleDislike = { viewModel.toggleDislike(it) },
                                onOpenComments = { shortVideo ->
                                    viewModel.openVideo(shortVideo)
                                    viewModel.setCommentsSheetOpen(true)
                                },
                                onToggleSubscribe = { channelId, isSub ->
                                    viewModel.toggleSubscription(channelId, isSub)
                                },
                                onChannelClick = { id, name, avatar, handle ->
                                    viewModel.openCreatorProfileById(id, name, avatar, handle)
                                },
                                onUploadClick = { viewModel.setUploadDialogOpen(true) }
                            )
                        }

                        MainTab.SUBSCRIPTIONS -> {
                            SubscriptionsScreen(
                                subscribedCreators = subscribedCreators,
                                allCreators = allCreators,
                                allVideos = homeVideos,
                                onVideoClick = { viewModel.openVideo(it) },
                                onChannelClick = { id, name, avatar, handle ->
                                    viewModel.openCreatorProfileById(id, name, avatar, handle)
                                },
                                onToggleSubscribe = { channelId, isSub ->
                                    viewModel.toggleSubscription(channelId, isSub)
                                },
                                onToggleSave = { viewModel.toggleSave(it) },
                                onToggleLike = { viewModel.toggleLike(it) }
                            )
                        }

                        MainTab.YOU -> {
                            UserProfileScreen(
                                userProfile = userProfile,
                                userUploadedVideos = userUploadedVideos,
                                likedVideos = likedVideos,
                                savedVideos = savedVideos,
                                allVideos = homeVideos,
                                isEditProfileOpen = isEditProfileOpen,
                                onEditProfileOpenChange = { viewModel.setEditProfileOpen(it) },
                                onUpdateProfile = { name, handle, bio, avatar ->
                                    viewModel.updateUserProfile(name, handle, bio, avatar)
                                },
                                onVideoClick = { viewModel.openVideo(it) },
                                onUploadClick = { viewModel.setUploadDialogOpen(true) }
                            )
                        }
                    }
                }
            }

            // Upload Video Modal Dialog
            if (isUploadDialogOpen) {
                UploadVideoDialog(
                    onDismiss = { viewModel.setUploadDialogOpen(false) },
                    onUpload = { title, desc, category, tags, isShort, duration, url, thumb ->
                        viewModel.uploadVideo(
                            title = title,
                            description = desc,
                            category = category,
                            tags = tags,
                            isShort = isShort,
                            durationSeconds = duration,
                            videoUrl = url,
                            thumbnailUrl = thumb
                        )
                    }
                )
            }

            // Comments Bottom Sheet
            if (isCommentsSheetOpen && activeVideo != null) {
                CommentsBottomSheet(
                    comments = activeVideoComments,
                    currentUser = userProfile,
                    onDismiss = { viewModel.setCommentsSheetOpen(false) },
                    onAddComment = { text ->
                        viewModel.addComment(activeVideo!!.id, text)
                    },
                    onToggleCommentLike = { comment ->
                        viewModel.toggleCommentLike(comment)
                    }
                )
            }
        }
    }
}
