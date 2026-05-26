package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.data.db.AppDatabase
import com.example.data.repository.MedicalRepository
import com.example.presentation.MainViewModel
import com.example.presentation.chat.ChatViewModel
import com.example.presentation.dashboard.DashboardScreen
import com.example.presentation.auth.AuthFlowContainer
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Room Database, repositories, and local view models sequentially
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = MedicalRepository(database)
        
        val mainViewModel = MainViewModel(repository)
        val chatViewModel = ChatViewModel(repository)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Read from MutableStateFlow
                val isLoggedIn by mainViewModel.isUserLoggedIn.collectAsState()

                Crossfade(targetState = isLoggedIn, label = "auth_home_crossfade") { loggedIn ->
                    if (loggedIn) {
                        DashboardScreen(
                            viewModel = mainViewModel,
                            chatViewModel = chatViewModel
                        )
                    } else {
                        AuthFlowContainer(
                            viewModel = mainViewModel,
                            onAuthSuccess = {
                                // Handled dynamically via mainViewModel state changes
                            }
                        )
                    }
                }
            }
        }
    }
}
