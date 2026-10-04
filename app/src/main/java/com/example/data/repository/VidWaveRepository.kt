package com.example.data.repository

import com.example.data.local.CommentDao
import com.example.data.local.CreatorDao
import com.example.data.local.UserProfileDao
import com.example.data.local.VideoDao
import com.example.data.local.VidWaveDatabase
import com.example.data.model.CommentEntity
import com.example.data.model.CreatorEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.VideoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class VidWaveRepository(
    private val database: VidWaveDatabase,
    private val videoDao: VideoDao = database.videoDao(),
    private val creatorDao: CreatorDao = database.creatorDao(),
    private val commentDao: CommentDao = database.commentDao(),
    private val userDao: UserProfileDao = database.userProfileDao()
) {
    val longVideos: Flow<List<VideoEntity>> = videoDao.getAllLongVideos()
    val shorts: Flow<List<VideoEntity>> = videoDao.getAllShorts()
    val allVideos: Flow<List<VideoEntity>> = videoDao.getAllVideos()
    val likedVideos: Flow<List<VideoEntity>> = videoDao.getLikedVideos()
    val savedVideos: Flow<List<VideoEntity>> = videoDao.getSavedVideos()
    val userUploadedVideos: Flow<List<VideoEntity>> = videoDao.getUserUploadedVideos()
    val allCreators: Flow<List<CreatorEntity>> = creatorDao.getAllCreators()
    val subscribedCreators: Flow<List<CreatorEntity>> = creatorDao.getSubscribedCreators()
    val userProfile: Flow<UserProfileEntity?> = userDao.getUserProfile()

    suspend fun checkAndSeedIfNeeded() = withContext(Dispatchers.IO) {
        if (videoDao.getVideosCount() == 0 || creatorDao.getCreatorsCount() == 0) {
            VidWaveDatabase.populateInitialData(database)
        }
    }

    fun getVideoById(id: Long): Flow<VideoEntity?> = videoDao.getVideoById(id)

    fun getVideosByChannel(channelId: String): Flow<List<VideoEntity>> =
        videoDao.getVideosByChannel(channelId)

    fun getCreatorById(channelId: String): Flow<CreatorEntity?> =
        creatorDao.getCreatorById(channelId)

    fun getCommentsForVideo(videoId: Long): Flow<List<CommentEntity>> =
        commentDao.getCommentsForVideo(videoId)

    fun searchVideos(query: String): Flow<List<VideoEntity>> =
        videoDao.searchVideos(query)

    fun getVideosByCategory(category: String): Flow<List<VideoEntity>> =
        if (category == "All") videoDao.getAllLongVideos() else videoDao.getVideosByCategory(category)

    suspend fun recordView(videoId: Long) = withContext(Dispatchers.IO) {
        videoDao.incrementViews(videoId)
    }

    suspend fun toggleLike(videoId: Long, currentlyLiked: Boolean) = withContext(Dispatchers.IO) {
        val newLiked = !currentlyLiked
        val delta = if (newLiked) 1 else -1
        videoDao.updateLikeStatus(videoId, newLiked, delta)
    }

    suspend fun toggleDislike(videoId: Long, currentlyDisliked: Boolean) = withContext(Dispatchers.IO) {
        videoDao.updateDislikeStatus(videoId, !currentlyDisliked)
    }

    suspend fun toggleSave(videoId: Long, currentlySaved: Boolean) = withContext(Dispatchers.IO) {
        videoDao.updateSavedStatus(videoId, !currentlySaved)
    }

    suspend fun toggleSubscription(channelId: String, currentlySubscribed: Boolean) = withContext(Dispatchers.IO) {
        val newSubscribed = !currentlySubscribed
        val delta = if (newSubscribed) 1 else -1
        creatorDao.updateSubscription(channelId, newSubscribed, delta)
    }

    suspend fun addComment(
        videoId: Long,
        text: String,
        authorName: String,
        authorHandle: String,
        authorAvatarUrl: String
    ): Long = withContext(Dispatchers.IO) {
        val comment = CommentEntity(
            videoId = videoId,
            authorName = authorName.ifBlank { "You" },
            authorAvatarUrl = authorAvatarUrl,
            authorHandle = authorHandle.ifBlank { "@user" },
            text = text.trim(),
            timestamp = System.currentTimeMillis(),
            likes = 0,
            isLiked = false
        )
        commentDao.insertComment(comment)
    }

    suspend fun toggleCommentLike(commentId: Long, currentlyLiked: Boolean) = withContext(Dispatchers.IO) {
        val newLiked = !currentlyLiked
        val delta = if (newLiked) 1 else -1
        commentDao.updateCommentLike(commentId, newLiked, delta)
    }

    suspend fun uploadVideo(
        title: String,
        description: String,
        category: String,
        tags: String,
        isShort: Boolean,
        durationSeconds: Int,
        videoUrl: String,
        thumbnailUrl: String
    ): Long = withContext(Dispatchers.IO) {
        val currentUser = userDao.getUserProfile().firstOrNull() ?: UserProfileEntity()
        val video = VideoEntity(
            title = title.trim(),
            description = description.trim(),
            channelId = "user_self",
            channelName = currentUser.name,
            channelAvatarUrl = currentUser.avatarUrl,
            channelHandle = currentUser.handle,
            videoUrl = videoUrl.ifBlank { "sample_stream" },
            thumbnailUrl = thumbnailUrl.ifBlank { if (isShort) "short_thumb_tech" else "thumb_tech" },
            durationSeconds = if (isShort) durationSeconds.coerceIn(5, 60) else durationSeconds.coerceAtLeast(30),
            isShort = isShort,
            views = 1,
            likes = 0,
            dislikes = 0,
            isLiked = false,
            uploadTimestamp = System.currentTimeMillis(),
            category = category.ifBlank { "General" },
            tags = tags.trim()
        )
        videoDao.insertVideo(video)
    }

    suspend fun updateUserProfile(
        name: String,
        handle: String,
        bio: String,
        avatarUrl: String
    ) = withContext(Dispatchers.IO) {
        val profile = UserProfileEntity(
            id = 1,
            name = name.trim(),
            handle = if (handle.startsWith("@")) handle.trim() else "@${handle.trim()}",
            bio = bio.trim(),
            avatarUrl = avatarUrl.trim()
        )
        userDao.insertOrUpdateProfile(profile)
    }
}
