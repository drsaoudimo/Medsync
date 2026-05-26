package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MedUser(
    val id: String,
    val name: String,
    val email: String,
    val avatar: String, // Emoji or icon name
    val role: String,   // Doctor, Pharmacist, Nurse, Patient, Student, Admin
    val specialization: String = "",
    val isVerified: Boolean = false,
    val followersCount: Int = 120,
    val followingCount: Int = 85,
    val bio: String = "",
    val isOnline: Boolean = true
)

@Serializable
data class Comment(
    val id: String,
    val authorName: String,
    val authorRole: String,
    val authorAvatar: String,
    val content: String,
    val timestamp: Long
)

@Serializable
data class Post(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorRole: String,
    val authorAvatar: String,
    val isAuthorVerified: Boolean,
    val content: String,
    val mediaUrl: String? = null,
    val mediaType: String? = null, // "IMAGE", "VIDEO", "PDF", "VOICE", "POLL"
    val timestamp: Long,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val reactions: Map<String, Int> = emptyMap(), // "upvote", "love", "insightful" 
    val commentsList: List<Comment> = emptyList(),
    val isPoll: Boolean = false,
    val pollOptions: List<String> = emptyList(),
    val pollVotes: List<Int> = emptyList(), // votes per option
    val votedOptionIndex: Int? = null,
    val isFlagged: Boolean = false,
    val toxicityScore: Float = 0.05f
)

@Serializable
data class Story(
    val id: String,
    val authorName: String,
    val authorAvatar: String,
    val isDoctor: Boolean,
    val textOverlay: String,
    val colorHex: String = "#14b8a6", // default teal
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class Message(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String,
    val content: String,
    val timestamp: Long,
    val isVoice: Boolean = false,
    val mediaUrl: String? = null,
    val isRead: Boolean = false
)

@Serializable
data class CommunityGroup(
    val id: String,
    val name: String,
    val description: String,
    val category: String, // "CLINICAL" (Doctor-only), "PUBLIC" (Everyone), "PHARMACY"
    val membersCount: Int,
    val isJoined: Boolean = false,
    val postsCount: Int = 12
)
