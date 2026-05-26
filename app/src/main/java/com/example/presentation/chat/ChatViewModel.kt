package com.example.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.ChatMessageEntity
import com.example.data.repository.MedicalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: MedicalRepository
) : ViewModel() {

    val chatHistory = repository.chatHistory

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    fun sendMessage(text: String) {
        if (text.trim().isEmpty()) return
        
        viewModelScope.launch {
            _isSending.value = true
            // Save user message to database
            repository.addChatMessage(ChatMessageEntity(sender = "user", text = text))
            
            try {
                // Call Gemini via repository
                val response = repository.callGeminiAPI(text)
                // Save AI answer to database
                repository.addChatMessage(ChatMessageEntity(sender = "ai", text = response))
            } catch (e: Exception) {
                repository.addChatMessage(
                    ChatMessageEntity(sender = "ai", text = "Sorry, I encountered an internal exception: ${e.message}")
                )
            } finally {
                _isSending.value = false
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            // Add initial welcome greeting back after clearing
            repository.addChatMessage(
                ChatMessageEntity(
                    sender = "ai",
                    text = "Welcome to MedSync AI Medical Assistant. I am here to assist with symptom analysis, drug checking, lab report interpretation, and clinical information based on general medical data. How can I help you today?"
                )
            )
        }
    }
}
