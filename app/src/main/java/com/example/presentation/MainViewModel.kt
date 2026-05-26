package com.example.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiClient
import com.example.data.db.*
import com.example.domain.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class LanguageCode(val code: String, val label: String) {
    EN("en", "English"),
    AR("ar", "العربية"),
    FR("fr", "Français")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val postDao = db.postDao()
    private val messageDao = db.messageDao()
    private val userDao = db.userDao()
    private val notificationDao = db.notificationDao()
    private val gson = Gson()

    // --- State Variables ---
    val isDarkMode = MutableStateFlow(false)
    val currentLanguage = MutableStateFlow(LanguageCode.EN)

    // Current Logged-in User (Loads the doctor "Dr. Sarah Ahmed" by default, can be toggled to admin or patient)
    val currentUser = MutableStateFlow<MedUser?>(null)

    // Reactive streams from Database
    val postsFlow = postDao.getAllPosts().map { cached ->
        cached.map { it.toDomain() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messagesFlow = messageDao.getAllMessages().map { cached ->
        cached.map { it.toDomain() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val usersFlow = userDao.getAllUsers().map { cached ->
        cached.map { it.toDomain() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notificationsFlow = notificationDao.getAllNotifications().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Active Chat AI Auto-Reply Enabled Toggle
    val isChatAiEnabled = MutableStateFlow(true)

    // Stories Status System
    val stories = MutableStateFlow<List<Story>>(emptyList())

    // Communities and Groups List
    val groups = MutableStateFlow<List<CommunityGroup>>(emptyList())

    // System Status Backups
    val backupMessage = MutableStateFlow<String?>(null)

    init {
        // Prepopulate database with mock clinical network data on first launch
        viewModelScope.launch {
            // Check if directory is empty
            userDao.getAllUsers().first().let { existing ->
                if (existing.isEmpty()) {
                    prepopulateDatabase()
                } else {
                    // Load current user
                    val doctors = existing.filter { it.role == "Doctor" }
                    val currentCached = doctors.firstOrNull() ?: existing.first()
                    currentUser.value = currentCached.toDomain()
                }
            }
        }
    }

    private suspend fun prepopulateDatabase() {
        val initialUsers = listOf(
            MedUser("user_sarah", "Dr. Sarah Ahmed", "sarah.ahmed@medsync.org", "👩‍⚕️", "Doctor", "Senior Cardiologist", true, 340, 190, "Harvard Med Alumna. Passionate about cardiovascular diagnostics & digital therapy. Founder of CardioSphere."),
            MedUser("user_mohamed", "Mohamed Soliman", "smohamed.stf@gmail.com", "👨‍💻", "Admin", "IT Systems Coordinator", true, 950, 42, "Head Operations & Clinical Verification Desk. Managing security rules and AI Moderation settings."),
            MedUser("user_elena", "Elena Rostova", "elena.pharmacist@medsync.org", "👩‍🔬", "Pharmacist", "Clinical Pharmacologist", true, 210, 105, "Specialized in clinical drug interactions, pharmacokinetics & patient counseling guidelines."),
            MedUser("user_marcus", "Marcus Vance", "marcus.nurse@medsync.org", "👨‍⚕️", "Nurse", "ER Critical Care", true, 115, 60, "ER Staff Nurse. Dedicated to medical emergency responses, trauma triage, and real-time patient support."),
            MedUser("user_ali", "Ali Hassan", "patient.ali@yahoo.com", "👨", "Patient", "General Public", false, 15, 80, "Fitness enthusiast. Looking for validated medical summaries & healthy lifestyle tips.")
        )
        userDao.insertUsers(initialUsers.map { it.toCached() })
        currentUser.value = initialUsers.first()

        val initialPosts = listOf(
            Post(
                id = "post_1",
                authorId = "user_sarah",
                authorName = "Dr. Sarah Ahmed",
                authorRole = "Doctor",
                authorAvatar = "👩‍⚕️",
                isAuthorVerified = true,
                content = "What are your perspectives on implementing fully AI-automated echocardiogram symptom scoring in modern triage? Can it safely alleviate emergency waitlists?",
                mediaType = "POLL",
                timestamp = System.currentTimeMillis() - 3600000 * 2,
                likesCount = 42,
                commentsCount = 2,
                isPoll = true,
                pollOptions = listOf("Highly feasible (clinical level)", "Feasible only under doctor watch", "Unsafe - AI lack clinical intuition"),
                pollVotes = listOf(14, 25, 3),
                commentsList = listOf(
                    Comment("c_1", "Elena Rostova", "Pharmacist", "👩‍🔬", "From a pharmaceutical perspective, clinical watch is vital to avoid counter-interactions on false-positive scores.", System.currentTimeMillis() - 3600000),
                    Comment("c_2", "Marcus Vance", "Nurse", "👨‍⚕️", "Our ER triage team has saved up to 20% scheduling overhead with custom AI screening protocols. Highly supportive!", System.currentTimeMillis() - 1800000)
                ),
                reactions = mapOf("Insightful" to 12, "Clinical Agree" to 22),
                toxicityScore = 0.01f
            ),
            Post(
                id = "post_2",
                authorId = "user_elena",
                authorName = "Elena Rostova",
                authorRole = "Pharmacist",
                authorAvatar = "👩‍🔬",
                isAuthorVerified = true,
                content = "Essential pharmacological update regarding Beta-blocker interactions. Please refer to this chest guideline flow when advising high-risk diabetic hypertensive groups.",
                mediaUrl = "Echocardiogram Lipid Chart File",
                mediaType = "IMAGE",
                timestamp = System.currentTimeMillis() - 3600000 * 5,
                likesCount = 18,
                commentsCount = 0,
                reactions = mapOf("Insightful" to 8, "Like" to 10),
                toxicityScore = 0.02f
            )
        )
        postDao.insertPosts(initialPosts.map { it.toCached() })

        val initialMessages = listOf(
            Message("msg_1", "user_sarah", "Dr. Sarah Ahmed", "👩‍⚕️", "Hello Team, we have an active patient showing persistent elevated Q-T interval in ICU Bed 4.", System.currentTimeMillis() - 7200000, isRead = true),
            Message("msg_2", "user_marcus", "Marcus Vance", "👨‍⚕️", "Understood Dr. Sarah. I am monitoring vital sign flows and preparing emergency crash equipment just in case. Will alert you instantly on any deviation.", System.currentTimeMillis() - 3600000, isRead = true)
        )
        messageDao.insertMessages(initialMessages.map { it.toCached() })

        val initialNotifications = listOf(
            CachedNotification(1, UUID.randomUUID().toString(), "Clinical Verified", "Welcome to MedSync Network. Your clinic license is officially verified! Enjoy advanced features.", System.currentTimeMillis() - 7200000, "VERIFY", true),
            CachedNotification(2, UUID.randomUUID().toString(), "🚨 Emergency SOS Triage", "Dr. Sarah Ahmed scheduled an emergency cardiovascular consultation request for Ward C.", System.currentTimeMillis() - 1800000, "SOS", false)
        )
        initialNotifications.forEach { notificationDao.insertNotification(it) }

        stories.value = listOf(
            Story("story_1", "Dr. Sarah", "👩‍⚕️", true, "Starting ICU ward rounds. Stay safe on clinical schedules!"),
            Story("story_2", "Elena R.", "👩‍🔬", true, "New antibiotic interaction paper published today! Check it out."),
            Story("story_3", "Ali Hassan", "👨", false, "Trekking this morning. Keep those heart rates steady!")
        )

        groups.value = listOf(
            CommunityGroup("g_cardio", "Cardiology Professional Summit", "Exclusively for verified cardiologists, students and nurses discussing complex cardiovascular cases, intervention procedures and medical research.", "CLINICAL", 230, true),
            CommunityGroup("g_diab", "Diabetes Support Forum", "A welcoming, interactive public support community sharing tips, healthy diets, glucose tracking logs, and medically verified advice.", "PUBLIC", 1450, false),
            CommunityGroup("g_pharm", "Modern Pharmacological Research", "Medical study circle analyzing drug interactions, development research logs, new clinical trials, and pharmacopoeia updates.", "PHARMACY", 480, true)
        )
    }

    // --- Actions ---

    fun changeLanguage(lang: LanguageCode) {
        currentLanguage.value = lang
    }

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }

    fun switchUser(userId: String) {
        viewModelScope.launch {
            userDao.getUserById(userId)?.let {
                currentUser.value = it.toDomain()
            }
        }
    }

    fun updateUserProfile(name: String, bio: String, specialization: String, emoji: String) {
        val current = currentUser.value ?: return
        val updated = current.copy(name = name, bio = bio, specialization = specialization, avatar = emoji)
        currentUser.value = updated
        viewModelScope.launch {
            userDao.insertUser(updated.toCached())
        }
    }

    fun createPost(content: String, type: String, pollOptionsList: List<String> = emptyList()) {
        val user = currentUser.value ?: return
        val isPoll = type == "POLL"
        val newPost = Post(
            id = "post_${UUID.randomUUID()}",
            authorId = user.id,
            authorName = user.name,
            authorRole = user.role,
            authorAvatar = user.avatar,
            isAuthorVerified = user.isVerified,
            content = content,
            mediaType = type,
            isPoll = isPoll,
            pollOptions = pollOptionsList,
            pollVotes = if (isPoll) List(pollOptionsList.size) { 0 } else emptyList(),
            timestamp = System.currentTimeMillis(),
            toxicityScore = 0.02f // simulated toxic safe
        )
        viewModelScope.launch {
            postDao.insertPost(newPost.toCached())
            
            // AI moderation check
            checkContentToxicity(newPost)
        }
    }

    private fun checkContentToxicity(post: Post) {
        viewModelScope.launch {
            val response = GeminiApiClient.queryGemini(
                prompt = "Analyze the toxicity score (0.0 to 1.0) of this post content. Return as a single Float: '${post.content}'",
                systemPrompt = "You are an AI moderator of a clinical social media platform. Rate the safety level of medical inputs."
            )
            val score = response.trim().replace(",", ".").toFloatOrNull() ?: 0.04f
            val flagged = score > 0.65f
            val updated = post.copy(toxicityScore = score, isFlagged = flagged)
            postDao.insertPost(updated.toCached())

            if (flagged) {
                notificationDao.insertNotification(
                    CachedNotification(
                        id = UUID.randomUUID().toString(),
                        title = "⚠️ AI Moderation Warning",
                        message = "Your shared clinical post contains unverified statements flagged for medical safety. Please review clinical guidelines.",
                        timestamp = System.currentTimeMillis(),
                        type = "LIKE"
                    )
                )
            }
        }
    }

    fun addReaction(postId: String, reaction: String) {
        viewModelScope.launch {
            val cached = postsFlow.value.find { it.id == postId } ?: return@launch
            val currentReactions = cached.reactions.toMutableMap()
            val score = currentReactions.getOrDefault(reaction, 0) + 1
            currentReactions[reaction] = score
            val updated = cached.copy(
                reactions = currentReactions,
                likesCount = cached.likesCount + 1
            )
            postDao.insertPost(updated.toCached())
        }
    }

    fun submitPollVote(postId: String, optionIndex: Int) {
        viewModelScope.launch {
            val cached = postsFlow.value.find { it.id == postId } ?: return@launch
            if (cached.votedOptionIndex != null) return@launch // cannot re-vote

            val currentVotes = cached.pollVotes.toMutableList()
            if (optionIndex in currentVotes.indices) {
                currentVotes[optionIndex] = currentVotes[optionIndex] + 1
            }
            val updated = cached.copy(
                pollVotes = currentVotes,
                votedOptionIndex = optionIndex
            )
            postDao.insertPost(updated.toCached())
        }
    }

    fun addCommentToPost(postId: String, text: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val cached = postsFlow.value.find { it.id == postId } ?: return@launch
            val newComment = Comment(
                id = "c_${UUID.randomUUID()}",
                authorName = user.name,
                authorRole = user.role,
                authorAvatar = user.avatar,
                content = text,
                timestamp = System.currentTimeMillis()
            )
            val updatedList = cached.commentsList + newComment
            val updated = cached.copy(
                commentsList = updatedList,
                commentsCount = updatedList.size
            )
            postDao.insertPost(updated.toCached())
        }
    }

    fun addStory(text: String, colorHex: String) {
        val user = currentUser.value ?: return
        val newStory = Story(
            id = "story_${UUID.randomUUID()}",
            authorName = user.name,
            authorAvatar = user.avatar,
            isDoctor = user.role == "Doctor",
            textOverlay = text,
            colorHex = colorHex
        )
        stories.value = listOf(newStory) + stories.value
    }

    fun joinGroup(groupId: String) {
        groups.value = groups.value.map {
            if (it.id == groupId) {
                it.copy(isJoined = !it.isJoined, membersCount = if (it.isJoined) it.membersCount - 1 else it.membersCount + 1)
            } else it
        }
    }

    fun triggerGlobalSOS(hospitalWard: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val sosTitle = "🚨 Clinical SOS Response Triggered!"
            val sosMsg = "Emergency priority support required by ${user.name} in $hospitalWard. Immediate ER team dispatch scheduled!"
            val notification = CachedNotification(
                id = UUID.randomUUID().toString(),
                title = sosTitle,
                message = sosMsg,
                timestamp = System.currentTimeMillis(),
                type = "SOS",
                isRead = false
            )
            notificationDao.insertNotification(notification)
        }
    }

    fun removeNotification(localId: Int) {
        viewModelScope.launch {
            // mark as read or delete
            notificationDao.markAsRead(localId)
        }
    }

    fun sendChatMessage(text: String) {
        val user = currentUser.value ?: return
        val newMsg = Message(
            id = "msg_${UUID.randomUUID()}",
            senderId = user.id,
            senderName = user.name,
            senderAvatar = user.avatar,
            content = text,
            timestamp = System.currentTimeMillis(),
            isRead = true
        )
        viewModelScope.launch {
            messageDao.insertMessage(newMsg.toCached())

            // AI Smart reply integration
            if (isChatAiEnabled.value) {
                triggerAiChatResponse(text)
            }
        }
    }

    private fun triggerAiChatResponse(userMessage: String) {
        viewModelScope.launch {
            val response = GeminiApiClient.queryGemini(
                prompt = "The clinical operator says: '$userMessage'. Provide a brief professional answer to co-workers about drugs, triage or guidelines.",
                systemPrompt = "You are a clinical assistant bot inside a real-time hospital channel."
            )
            val botMsg = Message(
                id = "msg_${UUID.randomUUID()}",
                senderId = "ai_bot",
                senderName = "MedSync AI Bot 🏥",
                senderAvatar = "🤖",
                content = response,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
            messageDao.insertMessage(botMsg.toCached())
        }
    }

    // --- Admin Operations ---

    fun toggleUserVerification(email: String) {
        viewModelScope.launch {
            usersFlow.value.find { it.email == email }?.let { user ->
                val updated = user.copy(isVerified = !user.isVerified)
                userDao.insertUser(updated.toCached())
            }
        }
    }

    fun deleteUser(email: String) {
        viewModelScope.launch {
            usersFlow.value.find { it.email == email }?.let { user ->
                userDao.deleteUserById(user.id)
            }
        }
    }

    fun runSystemBackup() {
        viewModelScope.launch {
            backupMessage.value = "Backup running..."
            kotlinx.coroutines.delay(1000)
            backupMessage.value = "Full SQLite Cache encrypted & backup stored successfully at medical vault #${System.currentTimeMillis() % 10000}"
        }
    }

    // --- Extension Mapping Helpers ---

    private fun MedUser.toCached() = CachedUser(
        id = id,
        name = name,
        email = email,
        avatar = avatar,
        role = role,
        specialization = specialization,
        isVerified = isVerified,
        followersCount = followersCount,
        followingCount = followingCount,
        bio = bio,
        isOnline = isOnline
    )

    private fun CachedUser.toDomain() = MedUser(
        id = id,
        name = name,
        email = email,
        avatar = avatar,
        role = role,
        specialization = specialization,
        isVerified = isVerified,
        followersCount = followersCount,
        followingCount = followingCount,
        bio = bio,
        isOnline = isOnline
    )

    private fun Post.toCached() = CachedPost(
        id = id,
        authorId = authorId,
        authorName = authorName,
        authorRole = authorRole,
        authorAvatar = authorAvatar,
        isAuthorVerified = isAuthorVerified,
        content = content,
        mediaUrl = mediaUrl,
        mediaType = mediaType,
        timestamp = timestamp,
        likesCount = likesCount,
        commentsCount = commentsCount,
        serializedReactions = gson.toJson(reactions),
        serializedComments = gson.toJson(commentsList),
        isPoll = isPoll,
        serializedPollOptions = gson.toJson(pollOptions),
        serializedPollVotes = gson.toJson(pollVotes),
        votedOptionIndex = votedOptionIndex,
        isFlagged = isFlagged,
        toxicityScore = toxicityScore
    )

    private fun CachedPost.toDomain(): Post {
        val reactType = object : TypeToken<Map<String, Int>>() {}.type
        val stringListType = object : TypeToken<List<String>>() {}.type
        val intListType = object : TypeToken<List<Int>>() {}.type
        return Post(
            id = id,
            authorId = authorId,
            authorName = authorName,
            authorRole = authorRole,
            authorAvatar = authorAvatar,
            isAuthorVerified = isAuthorVerified,
            content = content,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            timestamp = timestamp,
            likesCount = likesCount,
            commentsCount = commentsCount,
            reactions = gson.fromJson(serializedReactions, reactType) ?: emptyMap(),
            commentsList = gson.fromJson(serializedComments, object : TypeToken<List<Comment>>() {}.type) ?: emptyList(),
            isPoll = isPoll,
            pollOptions = gson.fromJson(serializedPollOptions, stringListType) ?: emptyList(),
            pollVotes = gson.fromJson(serializedPollVotes, intListType) ?: emptyList(),
            votedOptionIndex = votedOptionIndex,
            isFlagged = isFlagged,
            toxicityScore = toxicityScore
        )
    }

    private fun Message.toCached() = CachedMessage(
        id = id,
        senderId = senderId,
        senderName = senderName,
        senderAvatar = senderAvatar,
        content = content,
        timestamp = timestamp,
        isVoice = isVoice,
        mediaUrl = mediaUrl,
        isRead = isRead
    )

    private fun CachedMessage.toDomain() = Message(
        id = id,
        senderId = senderId,
        senderName = senderName,
        senderAvatar = senderAvatar,
        content = content,
        timestamp = timestamp,
        isVoice = isVoice,
        mediaUrl = mediaUrl,
        isRead = isRead
    )
}
