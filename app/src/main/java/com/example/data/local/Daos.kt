package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CommentEntity
import com.example.data.model.CreatorEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos WHERE isShort = 0 ORDER BY uploadTimestamp DESC")
    fun getAllLongVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isShort = 1 ORDER BY uploadTimestamp DESC")
    fun getAllShorts(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos ORDER BY uploadTimestamp DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id")
    fun getVideoById(id: Long): Flow<VideoEntity?>

    @Query("SELECT * FROM videos WHERE channelId = :channelId ORDER BY uploadTimestamp DESC")
    fun getVideosByChannel(channelId: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isLiked = 1 ORDER BY uploadTimestamp DESC")
    fun getLikedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isSaved = 1 ORDER BY uploadTimestamp DESC")
    fun getSavedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE channelId = 'user_self' ORDER BY uploadTimestamp DESC")
    fun getUserUploadedVideos(): Flow<List<VideoEntity>>

    @Query("""
        SELECT * FROM videos 
        WHERE title LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%' 
           OR tags LIKE '%' || :query || '%'
           OR channelName LIKE '%' || :query || '%'
        ORDER BY uploadTimestamp DESC
    """)
    fun searchVideos(query: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE category = :category AND isShort = 0 ORDER BY uploadTimestamp DESC")
    fun getVideosByCategory(category: String): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Query("UPDATE videos SET views = views + 1 WHERE id = :id")
    suspend fun incrementViews(id: Long)

    @Query("UPDATE videos SET isLiked = :liked, likes = likes + :likeDelta, isDisliked = 0 WHERE id = :id")
    suspend fun updateLikeStatus(id: Long, liked: Boolean, likeDelta: Int)

    @Query("UPDATE videos SET isDisliked = :disliked, isLiked = 0 WHERE id = :id")
    suspend fun updateDislikeStatus(id: Long, disliked: Boolean)

    @Query("UPDATE videos SET isSaved = :saved WHERE id = :id")
    suspend fun updateSavedStatus(id: Long, saved: Boolean)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideo(id: Long)

    @Query("SELECT COUNT(*) FROM videos")
    suspend fun getVideosCount(): Int
}

@Dao
interface CreatorDao {
    @Query("SELECT * FROM creators ORDER BY subscribersCount DESC")
    fun getAllCreators(): Flow<List<CreatorEntity>>

    @Query("SELECT * FROM creators WHERE isSubscribed = 1 ORDER BY name ASC")
    fun getSubscribedCreators(): Flow<List<CreatorEntity>>

    @Query("SELECT * FROM creators WHERE channelId = :channelId")
    fun getCreatorById(channelId: String): Flow<CreatorEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreator(creator: CreatorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreators(creators: List<CreatorEntity>)

    @Update
    suspend fun updateCreator(creator: CreatorEntity)

    @Query("UPDATE creators SET isSubscribed = :subscribed, subscribersCount = subscribersCount + :delta WHERE channelId = :channelId")
    suspend fun updateSubscription(channelId: String, subscribed: Boolean, delta: Int)

    @Query("SELECT COUNT(*) FROM creators")
    suspend fun getCreatorsCount(): Int
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY timestamp DESC")
    fun getCommentsForVideo(videoId: Long): Flow<List<CommentEntity>>

    @Query("SELECT COUNT(*) FROM comments WHERE videoId = :videoId")
    fun getCommentCountForVideo(videoId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)

    @Query("UPDATE comments SET isLiked = :liked, likes = likes + :delta WHERE id = :id")
    suspend fun updateCommentLike(id: Long, liked: Boolean, delta: Int)

    @Query("DELETE FROM comments WHERE id = :id")
    suspend fun deleteComment(id: Long)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)
}
