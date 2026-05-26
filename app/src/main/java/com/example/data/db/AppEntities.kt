package com.example.data.db

import androidx.room.*
import com.example.domain.model.Comment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "cached_posts")
data class CachedPost(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorName: String,
    val authorRole: String,
    val authorAvatar: String,
    val isAuthorVerified: Boolean,
    val content: String,
    val mediaUrl: String? = null,
    val mediaType: String? = null,
    val timestamp: Long,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val serializedReactions: String = "{}", // JSON Map
    val serializedComments: String = "[]",  // JSON List
    val isPoll: Boolean = false,
    val serializedPollOptions: String = "[]",
    val serializedPollVotes: String = "[]",
    val votedOptionIndex: Int? = null,
    val isFlagged: Boolean = false,
    val toxicityScore: Float = 0.05f
)

@Entity(tableName = "cached_messages")
data class CachedMessage(
    @PrimaryKey val id: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String,
    val content: String,
    val timestamp: Long,
    val isVoice: Boolean = false,
    val mediaUrl: String? = null,
    val isRead: Boolean = false
)

@Entity(tableName = "cached_users")
data class CachedUser(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val avatar: String,
    val role: String,
    val specialization: String = "",
    val isVerified: Boolean = false,
    val followersCount: Int = 120,
    val followingCount: Int = 85,
    val bio: String = "",
    val isOnline: Boolean = true
)

@Entity(tableName = "cached_notifications")
data class CachedNotification(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: String, // "LIKE", "COMMENT", "SOS", "VERIFY"
    val isRead: Boolean = false
)

// Simple JSON serialization converter for Room
class RoomConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromCommentsList(value: List<Comment>?): String {
        return gson.toJson(value ?: emptyList<Comment>())
    }

    @TypeConverter
    fun toCommentsList(value: String?): List<Comment> {
        val listType = object : TypeToken<List<Comment>>() {}.type
        return gson.fromJson(value ?: "[]", listType) ?: emptyList()
    }
}
