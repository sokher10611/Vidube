package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val channelId: String,
    val channelName: String,
    val channelAvatarUrl: String,
    val channelHandle: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val durationSeconds: Int,
    val isShort: Boolean,
    val views: Long = 0,
    val likes: Long = 0,
    val dislikes: Long = 0,
    val isLiked: Boolean = false,
    val isDisliked: Boolean = false,
    val isSaved: Boolean = false,
    val uploadTimestamp: Long = System.currentTimeMillis(),
    val category: String = "General",
    val tags: String = ""
)

@Entity(tableName = "creators")
data class CreatorEntity(
    @PrimaryKey val channelId: String,
    val name: String,
    val handle: String,
    val avatarUrl: String,
    val bannerUrl: String,
    val bio: String,
    val subscribersCount: Long = 0,
    val isSubscribed: Boolean = false,
    val totalVideos: Int = 0,
    val verified: Boolean = false
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoId: Long,
    val authorName: String,
    val authorAvatarUrl: String,
    val authorHandle: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val isLiked: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "You",
    val handle: String = "@creator_you",
    val avatarUrl: String = "",
    val bio: String = "Content creator, tech enthusiast & video explorer",
    val bannerUrl: String = ""
)
