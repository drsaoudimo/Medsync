package com.example.data.db

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM cached_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<CachedPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<CachedPost>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CachedPost)

    @Query("DELETE FROM cached_posts WHERE id = :postId")
    suspend fun deletePostById(postId: String)

    @Query("DELETE FROM cached_posts")
    suspend fun clearAllPosts()
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM cached_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<CachedMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CachedMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<CachedMessage>)

    @Query("DELETE FROM cached_messages")
    suspend fun clearMessages()
}

@Dao
interface UserDao {
    @Query("SELECT * FROM cached_users")
    fun getAllUsers(): Flow<List<CachedUser>>

    @Query("SELECT * FROM cached_users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): CachedUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<CachedUser>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: CachedUser)

    @Query("DELETE FROM cached_users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM cached_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<CachedNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: CachedNotification)

    @Query("UPDATE cached_notifications SET isRead = 1 WHERE localId = :localId")
    suspend fun markAsRead(localId: Int)

    @Query("DELETE FROM cached_notifications")
    suspend fun clearNotifications()
}

@Database(
    entities = [
        CachedPost::class,
        CachedMessage::class,
        CachedUser::class,
        CachedNotification::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medsync_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
