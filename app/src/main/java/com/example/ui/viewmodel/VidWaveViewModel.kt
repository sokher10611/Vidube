package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.VidWaveDatabase
import com.example.data.model.CommentEntity
import com.example.data.model.CreatorEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.VideoEntity
import com.example.data.repository.VidWaveRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    HOME,
    SHORTS,
    SUBSCRIPTIONS,
    YOU
}

class VidWaveViewModel(application: Application) : AndroidViewModel(application) {
    private val database = VidWaveDatabase.getDatabase(application, viewModelScope)
    private val repository = VidWaveRepository(database)

    // Current Navigation State
    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _activeVideo = MutableStateFlow<VideoEntity?>(null)
    val activeVideo: StateFlow<VideoEntity?> = _activeVideo.asStateFlow()

    private val _activeCreator = MutableStateFlow<CreatorEntity?>(null)
    val activeCreator: StateFlow<CreatorEntity?> = _activeCreator.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isUploadDialogOpen = MutableStateFlow(false)
    val isUploadDialogOpen: StateFlow<Boolean> = _isUploadDialogOpen.asStateFlow()

    private val _isCommentsSheetOpen = MutableStateFlow(false)
    val isCommentsSheetOpen: StateFlow<Boolean> = _isCommentsSheetOpen.asStateFlow()

    private val _isEditProfileOpen = MutableStateFlow(false)
    val isEditProfileOpen: StateFlow<Boolean> = _isEditProfileOpen.asStateFlow()

    // Shorts index
    private val _currentShortIndex = MutableStateFlow(0)
    val currentShortIndex: StateFlow<Int> = _currentShortIndex.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedIfNeeded()
        }
    }

    // Repository Flows
    val userProfile: StateFlow<UserProfileEntity> = repository.userProfile
        .map { it ?: UserProfileEntity() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserProfileEntity()
        )

    val allCreators: StateFlow<List<CreatorEntity>> = repository.allCreators
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val subscribedCreators: StateFlow<List<CreatorEntity>> = repository.subscribedCreators
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val shorts: StateFlow<List<VideoEntity>> = repository.shorts
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val likedVideos: StateFlow<List<VideoEntity>> = repository.likedVideos
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val savedVideos: StateFlow<List<VideoEntity>> = repository.savedVideos
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val userUploadedVideos: StateFlow<List<VideoEntity>> = repository.userUploadedVideos
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val homeVideos: StateFlow<List<VideoEntity>> = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category ->
        Pair(query, category)
    }.flatMapLatest { (query, category) ->
        if (query.isNotBlank()) {
            repository.searchVideos(query)
        } else {
            repository.getVideosByCategory(category)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeVideoComments: StateFlow<List<CommentEntity>> = _activeVideo
        .flatMapLatest { video ->
            if (video != null) {
                repository.getCommentsForVideo(video.id)
            } else {
                MutableStateFlow(emptyList())
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeCreatorVideos: StateFlow<List<VideoEntity>> = _activeCreator
        .flatMapLatest { creator ->
            if (creator != null) {
                repository.getVideosByChannel(creator.channelId)
            } else {
                MutableStateFlow(emptyList())
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Actions
    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelect(category: String) {
        _selectedCategory.value = category
    }

    fun openVideo(video: VideoEntity) {
        _activeVideo.value = video
        viewModelScope.launch {
            repository.recordView(video.id)
        }
    }

    fun closeVideo() {
        _activeVideo.value = null
        _isCommentsSheetOpen.value = false
    }

    fun openCreatorProfile(creator: CreatorEntity) {
        _activeCreator.value = creator
    }

    fun openCreatorProfileById(channelId: String, name: String, avatarUrl: String, handle: String) {
        val existing = allCreators.value.firstOrNull { it.channelId == channelId }
        _activeCreator.value = existing ?: CreatorEntity(
            channelId = channelId,
            name = name,
            handle = handle,
            avatarUrl = avatarUrl,
            bannerUrl = "banner_tech",
            bio = "Official channel for $name. Subscribe for daily videos!",
            subscribersCount = 100,
            isSubscribed = false,
            totalVideos = 1,
            verified = false
        )
    }

    fun closeCreatorProfile() {
        _activeCreator.value = null
    }

    fun toggleLike(video: VideoEntity) {
        viewModelScope.launch {
            val currentlyLiked = video.isLiked
            repository.toggleLike(video.id, currentlyLiked)
            // If active video is the one liked, update local reference
            if (_activeVideo.value?.id == video.id) {
                _activeVideo.value = _activeVideo.value?.copy(
                    isLiked = !currentlyLiked,
                    likes = if (!currentlyLiked) video.likes + 1 else (video.likes - 1).coerceAtLeast(0),
                    isDisliked = false
                )
            }
        }
    }

    fun toggleDislike(video: VideoEntity) {
        viewModelScope.launch {
            val currentlyDisliked = video.isDisliked
            repository.toggleDislike(video.id, currentlyDisliked)
            if (_activeVideo.value?.id == video.id) {
                _activeVideo.value = _activeVideo.value?.copy(
                    isDisliked = !currentlyDisliked,
                    isLiked = false
                )
            }
        }
    }

    fun toggleSave(video: VideoEntity) {
        viewModelScope.launch {
            val currentlySaved = video.isSaved
            repository.toggleSave(video.id, currentlySaved)
            if (_activeVideo.value?.id == video.id) {
                _activeVideo.value = _activeVideo.value?.copy(
                    isSaved = !currentlySaved
                )
            }
        }
    }

    fun toggleSubscription(channelId: String, currentlySubscribed: Boolean) {
        viewModelScope.launch {
            repository.toggleSubscription(channelId, currentlySubscribed)
            // Update active creator if open
            if (_activeCreator.value?.channelId == channelId) {
                val newSub = !currentlySubscribed
                _activeCreator.value = _activeCreator.value?.copy(
                    isSubscribed = newSub,
                    subscribersCount = if (newSub) (_activeCreator.value?.subscribersCount ?: 0) + 1 else ((_activeCreator.value?.subscribersCount ?: 1) - 1).coerceAtLeast(0)
                )
            }
        }
    }

    fun addComment(videoId: Long, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val user = userProfile.value
            repository.addComment(
                videoId = videoId,
                text = text,
                authorName = user.name,
                authorHandle = user.handle,
                authorAvatarUrl = user.avatarUrl
            )
        }
    }

    fun toggleCommentLike(comment: CommentEntity) {
        viewModelScope.launch {
            repository.toggleCommentLike(comment.id, comment.isLiked)
        }
    }

    fun uploadVideo(
        title: String,
        description: String,
        category: String,
        tags: String,
        isShort: Boolean,
        durationSeconds: Int,
        videoUrl: String,
        thumbnailUrl: String
    ) {
        viewModelScope.launch {
            repository.uploadVideo(
                title = title,
                description = description,
                category = category,
                tags = tags,
                isShort = isShort,
                durationSeconds = durationSeconds,
                videoUrl = videoUrl,
                thumbnailUrl = thumbnailUrl
            )
            _isUploadDialogOpen.value = false
        }
    }

    fun updateUserProfile(name: String, handle: String, bio: String, avatarUrl: String) {
        viewModelScope.launch {
            repository.updateUserProfile(name, handle, bio, avatarUrl)
            _isEditProfileOpen.value = false
        }
    }

    fun setShortIndex(index: Int) {
        _currentShortIndex.value = index
    }

    fun nextShort(maxCount: Int) {
        if (_currentShortIndex.value < maxCount - 1) {
            _currentShortIndex.value += 1
        }
    }

    fun previousShort() {
        if (_currentShortIndex.value > 0) {
            _currentShortIndex.value -= 1
        }
    }

    fun setUploadDialogOpen(open: Boolean) {
        _isUploadDialogOpen.value = open
    }

    fun setCommentsSheetOpen(open: Boolean) {
        _isCommentsSheetOpen.value = open
    }

    fun setEditProfileOpen(open: Boolean) {
        _isEditProfileOpen.value = open
    }
}
