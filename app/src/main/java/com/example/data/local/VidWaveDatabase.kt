package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CommentEntity
import com.example.data.model.CreatorEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.VideoEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        VideoEntity::class,
        CreatorEntity::class,
        CommentEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VidWaveDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun creatorDao(): CreatorDao
    abstract fun commentDao(): CommentDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: VidWaveDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): VidWaveDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VidWaveDatabase::class.java,
                    "vidwave_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: VidWaveDatabase) {
            val creatorDao = database.creatorDao()
            val videoDao = database.videoDao()
            val commentDao = database.commentDao()
            val userDao = database.userProfileDao()

            // 1. Initial User
            userDao.insertOrUpdateProfile(
                UserProfileEntity(
                    id = 1,
                    name = "Alex Vance",
                    handle = "@alexvance_live",
                    avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=200&q=80",
                    bio = "Tech creator, coding enthusiast, video storyteller. Welcome to my channel!",
                    bannerUrl = "banner_tech"
                )
            )

            // 2. Creators
            val initialCreators = listOf(
                CreatorEntity(
                    channelId = "c_nexus",
                    name = "Nexus Tech & AI",
                    handle = "@nexustech",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80",
                    bannerUrl = "banner_tech",
                    bio = "Exploring the edge of AI robotics, gadgets, and next-gen devices.",
                    subscribersCount = 1420000,
                    isSubscribed = true,
                    totalVideos = 248,
                    verified = true
                ),
                CreatorEntity(
                    channelId = "c_pulse",
                    name = "SoundPulse Sessions",
                    handle = "@soundpulse",
                    avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80",
                    bannerUrl = "banner_music",
                    bio = "Electronic, Synthwave, Lofi beats & live studio modular jam sessions.",
                    subscribersCount = 890000,
                    isSubscribed = false,
                    totalVideos = 180,
                    verified = true
                ),
                CreatorEntity(
                    channelId = "c_pixel",
                    name = "PixelRealm Gaming",
                    handle = "@pixelrealm",
                    avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=200&q=80",
                    bannerUrl = "banner_gaming",
                    bio = "Next-gen ray tracing gameplay, walkthroughs, mods & speedruns.",
                    subscribersCount = 2100000,
                    isSubscribed = true,
                    totalVideos = 512,
                    verified = true
                ),
                CreatorEntity(
                    channelId = "c_wander",
                    name = "Wanderlust Cinema",
                    handle = "@wanderlust_films",
                    avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=200&q=80",
                    bannerUrl = "banner_nature",
                    bio = "4K cinematic travel films around the globe with drone visuals.",
                    subscribersCount = 650000,
                    isSubscribed = false,
                    totalVideos = 94,
                    verified = false
                ),
                CreatorEntity(
                    channelId = "c_chef",
                    name = "Urban Taste Lab",
                    handle = "@urbantastelab",
                    avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80",
                    bannerUrl = "banner_cooking",
                    bio = "Street food re-imagined. Quick recipes, knife skills & culinary science.",
                    subscribersCount = 430000,
                    isSubscribed = false,
                    totalVideos = 145,
                    verified = true
                )
            )
            creatorDao.insertCreators(initialCreators)

            // 3. Initial Videos (Long-form and Shorts)
            val initialVideos = listOf(
                // Long-form videos
                VideoEntity(
                    id = 1,
                    title = "Building an Autonomous AI Agent from Scratch in 2026",
                    description = "In this deep dive, we build a local neural reasoning pipeline with memory buffers, tool usage dispatchers, and live multimodal vision streams. Complete source code and architecture breakdown included in the comments!",
                    channelId = "c_nexus",
                    channelName = "Nexus Tech & AI",
                    channelAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80",
                    channelHandle = "@nexustech",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    thumbnailUrl = "thumb_tech",
                    durationSeconds = 1142, // 19:02
                    isShort = false,
                    views = 384000,
                    likes = 28400,
                    dislikes = 310,
                    isLiked = true,
                    uploadTimestamp = System.currentTimeMillis() - (1000L * 60 * 60 * 18), // 18 hrs ago
                    category = "Tech",
                    tags = "AI, MachineLearning, Android, Kotlin, Tech"
                ),
                VideoEntity(
                    id = 2,
                    title = "Cyberpunk Neo-Tokyo: Unreal Engine 5.6 RTX 5090 Max Settings",
                    description = "Take a rainy neon stroll through Shinjuku 2099 rendered in full path tracing at 4K 120FPS. Testing lumen reflections, nanite foliage, and DLSS 4 frame reconstruction.",
                    channelId = "c_pixel",
                    channelName = "PixelRealm Gaming",
                    channelAvatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=200&q=80",
                    channelHandle = "@pixelrealm",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    thumbnailUrl = "thumb_gaming",
                    durationSeconds = 855, // 14:15
                    isShort = false,
                    views = 612000,
                    likes = 45900,
                    dislikes = 420,
                    isLiked = false,
                    uploadTimestamp = System.currentTimeMillis() - (1000L * 60 * 60 * 42), // 2 days ago
                    category = "Gaming",
                    tags = "Gaming, RTX, Cyberpunk, Graphics, UnrealEngine"
                ),
                VideoEntity(
                    id = 3,
                    title = "Midnight Synthwave Modular Live Jam - Tape Cassette Master",
                    description = "Direct analog recording from Prophet-6, Eurorack analog filter bank, and Roland TR-8S drum computer through vintage tape emulation. Pure late night vibes.",
                    channelId = "c_pulse",
                    channelName = "SoundPulse Sessions",
                    channelAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80",
                    channelHandle = "@soundpulse",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    thumbnailUrl = "thumb_music",
                    durationSeconds = 1830, // 30:30
                    isShort = false,
                    views = 195000,
                    likes = 17800,
                    dislikes = 110,
                    isLiked = false,
                    uploadTimestamp = System.currentTimeMillis() - (1000L * 60 * 60 * 75), // 3 days ago
                    category = "Music",
                    tags = "Music, Synthwave, Lofi, LiveJam, Synthesizer"
                ),
                VideoEntity(
                    id = 4,
                    title = "Norway Fjord Expedition: Solo Hiking the Arctic Ridge in 4K",
                    description = "7 days across the snow-capped fjords of Lofoten. Sleeping beneath the northern lights, navigating frozen glacial rivers, and capturing raw Scandinavian wilderness.",
                    channelId = "c_wander",
                    channelName = "Wanderlust Cinema",
                    channelAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=200&q=80",
                    channelHandle = "@wanderlust_films",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                    thumbnailUrl = "thumb_travel",
                    durationSeconds = 1420, // 23:40
                    isShort = false,
                    views = 520000,
                    likes = 39100,
                    dislikes = 190,
                    isLiked = true,
                    isSaved = true,
                    uploadTimestamp = System.currentTimeMillis() - (1000L * 60 * 60 * 120), // 5 days ago
                    category = "Travel",
                    tags = "Travel, Norway, Nature, Hiking, 4KCInematic"
                ),
                VideoEntity(
                    id = 5,
                    title = "The Perfect Crispy Pork Belly Bao Buns: 36-Hour Secret",
                    description = "Achieving glass-shatter crisp skin with melt-in-your-mouth slow braised pork belly, paired with homemade pickled cucumber, crushed roasted peanuts, and spicy hoisin glaze.",
                    channelId = "c_chef",
                    channelName = "Urban Taste Lab",
                    channelAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80",
                    channelHandle = "@urbantastelab",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                    thumbnailUrl = "thumb_food",
                    durationSeconds = 724, // 12:04
                    isShort = false,
                    views = 270000,
                    likes = 23400,
                    dislikes = 95,
                    isLiked = false,
                    uploadTimestamp = System.currentTimeMillis() - (1000L * 60 * 60 * 160),
                    category = "Food",
                    tags = "Food, Cooking, Recipe, StreetFood"
                ),

                // Shorts (Vertical short-form format)
                VideoEntity(
                    id = 101,
                    title = "Did you know this Android gesture shortcut exists?! 🤯 #Shorts #Tech",
                    description = "Quick hidden developer shortcut that saves 10 minutes every day!",
                    channelId = "c_nexus",
                    channelName = "Nexus Tech & AI",
                    channelAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80",
                    channelHandle = "@nexustech",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    thumbnailUrl = "short_thumb_tech",
                    durationSeconds = 34,
                    isShort = true,
                    views = 1250000,
                    likes = 112000,
                    dislikes = 1400,
                    isLiked = true,
                    uploadTimestamp = System.currentTimeMillis() - (1000L * 60 * 60 * 6),
                    category = "Tech",
                    tags = "Android, Tips, Shorts, TechHacks"
                ),
                VideoEntity(
                    id = 102,
                    title = "Crazy bass drop test with Eurorack analog filter! 🔊⚡ #Shorts",
                    description = "Watch the oscilloscope frequency spike on this resonant wave.",
                    channelId = "c_pulse",
                    channelName = "SoundPulse Sessions",
                    channelAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80",
                    channelHandle = "@soundpulse",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    thumbnailUrl = "short_thumb_music",
                    durationSeconds = 22,
                    isShort = true,
                    views = 890000,
                    likes = 78000,
                    dislikes = 800,
                    isLiked = false,
                    uploadTimestamp = System.currentTimeMillis() - (1000L * 60 * 60 * 14),
                    category = "Music",
                    tags = "Audio, Bass, Synth, Music"
                ),
                VideoEntity(
                    id = 103,
                    title = "Insane 1v4 Clutch with 1 HP remaining! 🎯🔥 #Gaming #Shorts",
                    description = "My hands were shaking after this flick shot round win.",
                    channelId = "c_pixel",
                    channelName = "PixelRealm Gaming",
                    channelAvatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=200&q=80",
                    channelHandle = "@pixelrealm",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
                    thumbnailUrl = "short_thumb_gaming",
                    durationSeconds = 45,
                    isShort = true,
                    views = 2400000,
                    likes = 230000,
                    dislikes = 2100,
                    isLiked = false,
                    uploadTimestamp = System.currentTimeMillis() - (1000L * 60 * 60 * 24),
                    category = "Gaming",
                    tags = "Gaming, Clutch, Esports, FPS"
                ),
                VideoEntity(
                    id = 104,
                    title = "Crispy crunch sound test! Wait till the end 🤤🥩 #Food #ASMR",
                    description = "Listen to that crisp on the pork belly crackling!",
                    channelId = "c_chef",
                    channelName = "Urban Taste Lab",
                    channelAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80",
                    channelHandle = "@urbantastelab",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
                    thumbnailUrl = "short_thumb_food",
                    durationSeconds = 18,
                    isShort = true,
                    views = 1680000,
                    likes = 145000,
                    dislikes = 900,
                    isLiked = true,
                    uploadTimestamp = System.currentTimeMillis() - (1000L * 60 * 60 * 30),
                    category = "Food",
                    tags = "Food, ASMR, Crunch, Recipe"
                )
            )
            videoDao.insertVideos(initialVideos)

            // 4. Initial Comments
            val initialComments = listOf(
                CommentEntity(
                    videoId = 1,
                    authorName = "Elena Rostova",
                    authorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80",
                    authorHandle = "@elenarostova",
                    text = "The explanation of the tool-calling token buffer at 12:45 is the cleanest explanation I've ever seen. Subscribed immediately!",
                    timestamp = System.currentTimeMillis() - (1000L * 60 * 60 * 12),
                    likes = 248,
                    isLiked = true
                ),
                CommentEntity(
                    videoId = 1,
                    authorName = "David Kim",
                    authorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80",
                    authorHandle = "@dkim_code",
                    text = "Did you benchmark this against local quantized weights on mobile hardware? Incredible speedup!",
                    timestamp = System.currentTimeMillis() - (1000L * 60 * 60 * 8),
                    likes = 89,
                    isLiked = false
                ),
                CommentEntity(
                    videoId = 1,
                    authorName = "Sophia Martinez",
                    authorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80",
                    authorHandle = "@sophiam",
                    text = "Love how beginner friendly you made this while keeping the code production ready. Keep it up!",
                    timestamp = System.currentTimeMillis() - (1000L * 60 * 60 * 2),
                    likes = 34,
                    isLiked = false
                ),
                CommentEntity(
                    videoId = 2,
                    authorName = "Marcus Brody",
                    authorAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80",
                    authorHandle = "@marcusb",
                    text = "Unreal Engine path tracing reflections on wet asphalt at night look more real than reality!",
                    timestamp = System.currentTimeMillis() - (1000L * 60 * 60 * 20),
                    likes = 412,
                    isLiked = true
                ),
                CommentEntity(
                    videoId = 101,
                    authorName = "Chloe Bennett",
                    authorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80",
                    authorHandle = "@chloeb",
                    text = "Been using Android for 8 years and literally never knew this. Mind blown!! 🤯",
                    timestamp = System.currentTimeMillis() - (1000L * 60 * 60 * 4),
                    likes = 1205,
                    isLiked = true
                )
            )
            commentDao.insertComments(initialComments)
        }
    }
}
