package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.*
import com.example.presentation.*
import com.example.data.api.GeminiApiClient
import com.example.ui.theme.MedSyncTheme
import com.example.ui.theme.TealPrimary
import com.example.utils.Loc
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: MainViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return MainViewModel(application) as T
                    }
                }
            )

            val isDark by appViewModel.isDarkMode.collectAsState()
            val language by appViewModel.currentLanguage.collectAsState()

            // Handle RTL layout direction for Arabic (AR)
            val layoutDirection = if (language == LanguageCode.AR) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MedSyncTheme(darkTheme = isDark) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MedSyncMainScreen(appViewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedSyncMainScreen(viewModel: MainViewModel) {
    val language by viewModel.currentLanguage.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val usersList by viewModel.usersFlow.collectAsState()
    
    // Notification badges
    val notifications by viewModel.notificationsFlow.collectAsState()
    val unreadAlertsCount = notifications.count { !it.isRead }

    var currentTab by remember { mutableStateOf("home") }
    var showSOSSheet by remember { mutableStateOf(false) }
    var showImpersonateDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = Loc.get(language, "app_title"),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = Loc.get(language, "app_subtitle"),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    // Impersonator switcher
                    IconButton(onClick = { showImpersonateDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Switch profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    // Dark Mode Toggle
                    IconButton(onClick = { viewModel.toggleDarkMode() }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Theme state",
                        )
                    }
                    
                    // Language cycler
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { expanded = true }) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Language")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            LanguageCode.entries.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang.label) },
                                    onClick = {
                                        viewModel.changeLanguage(lang)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                windowInsets = WindowInsets.safeDrawing
            )
        },
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val feedText = Loc.get(language, "tab_home").take(15)
                val assistantText = Loc.get(language, "tab_assistant").take(15)
                val chatText = Loc.get(language, "tab_messages").take(15)
                val circlesText = Loc.get(language, "tab_communities").take(15)
                val alertsText = Loc.get(language, "tab_notifications").take(15)
                val profileText = Loc.get(language, "tab_profile").take(15)

                NavigationBarItem(
                    selected = currentTab == "home",
                    onClick = { currentTab = "home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(feedText, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 9.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == "assistant",
                    onClick = { currentTab = "assistant" },
                    icon = { Icon(Icons.Default.Face, contentDescription = null) },
                    label = { Text(assistantText, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 9.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == "chat",
                    onClick = { currentTab = "chat" },
                    icon = { Icon(Icons.Default.MailOutline, contentDescription = null) },
                    label = { Text(chatText, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 9.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == "groups",
                    onClick = { currentTab = "groups" },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text(circlesText, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 9.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == "alerts",
                    onClick = { currentTab = "alerts" },
                    icon = { 
                        BadgedBox(badge = { if (unreadAlertsCount > 0) { Badge { Text("$unreadAlertsCount") } } }) {
                            Icon(Icons.Default.Notifications, contentDescription = null)
                        }
                    },
                    label = { Text(alertsText, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 9.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == "profile",
                    onClick = { currentTab = "profile" },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                    label = { Text(profileText, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 9.sp) }
                )
                if (currentUser?.role == "Admin") {
                    NavigationBarItem(
                        selected = currentTab == "admin",
                        onClick = { currentTab = "admin" },
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                        label = { Text("Ops Sync", maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 9.sp) }
                    )
                }
            }
        },
        floatingActionButton = {
            // General Clinical SOS Trigger button visible across main tabs except profile
            if (currentTab != "profile") {
                ExtendedFloatingActionButton(
                    onClick = { showSOSSheet = true },
                    icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White) },
                    text = { Text("CLINICAL SOS", color = Color.White, fontWeight = FontWeight.Bold) },
                    containerColor = Color(0xFFEF4444),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                "home" -> TabHome(viewModel, language)
                "assistant" -> TabAssistant(viewModel, language)
                "chat" -> TabChat(viewModel, language)
                "groups" -> TabGroups(viewModel, language)
                "alerts" -> TabNotifications(viewModel, language)
                "profile" -> TabProfile(viewModel, language)
                "admin" -> TabAdmin(viewModel, language)
            }
        }
    }

    // --- Switch Active Profile Impersonator Dialogue ---
    if (showImpersonateDialog) {
        AlertDialog(
            onDismissRequest = { showImpersonateDialog = false },
            title = { Text(Loc.get(language, "switch_user_label"), fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    usersList.forEach { user ->
                        val isSelected = currentUser?.id == user.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable {
                                    viewModel.switchUser(user.id)
                                    showImpersonateDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(user.avatar, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${user.role} • ${user.specialization.ifBlank { "General clinical" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImpersonateDialog = false }) { Text("Dismiss") }
            }
        )
    }

    // --- Urgent Clinical SOS dispatch sheet ---
    if (showSOSSheet) {
        AlertDialog(
            onDismissRequest = { showSOSSheet = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(36.dp)) },
            title = { Text(Loc.get(language, "sos_btn"), textAlign = TextAlign.Center, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEF4444)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = Loc.get(language, "sos_subtitle"),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    val departments = listOf("ICU Ward Bed C4", "Emergency Trauma Bay A", "Inpatient Cardiology Node", "Neonatal Triage Clinic")
                    var selectedDept by remember { mutableStateOf(departments.first()) }
                    
                    Text("Select Emergency Department Location:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        departments.forEach { d ->
                            val active = d == selectedDept
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) Color(0xFFFEE2E2) else Color.Transparent)
                                    .clickable { selectedDept = d }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = active, onClick = { selectedDept = d })
                                Text(d, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 6.dp), color = if (active) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.triggerGlobalSOS(selectedDept)
                            showSOSSheet = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("DISPATCH SOS TRIAGE NOW", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSOSSheet = false }) { Text("Cancel") }
            }
        )
    }
}

// ==========================================
// TAB 1: SOCIAL FEED & STORIES
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabHome(viewModel: MainViewModel, language: LanguageCode) {
    val posts by viewModel.postsFlow.collectAsState()
    val storiesList by viewModel.stories.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var postContent by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("TEXT") } // TEXT, IMAGE, PDF, POLL
    
    // Poll setup states
    var pollOpt1 by remember { mutableStateOf("") }
    var pollOpt2 by remember { mutableStateOf("") }
    
    // Comment dialogue sheet
    var activeCommentPostId by remember { mutableStateOf<String?>(null) }
    var commentText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Clinical Stories Row
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "Clinical Shift Logs & Statuses (Stories)",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Create story button
                    item {
                        var textStory by remember { mutableStateOf("") }
                        var showStoryDialog by remember { mutableStateOf(false) }
                        if (showStoryDialog) {
                            AlertDialog(
                                onDismissRequest = { showStoryDialog = false },
                                title = { Text("Add Shift Story") },
                                text = {
                                    OutlinedTextField(
                                        value = textStory,
                                        onValueChange = { textStory = it },
                                        placeholder = { Text("e.g. Ward duty starting shortly...") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                confirmButton = {
                                    Button(onClick = {
                                        if (textStory.isNotBlank()) {
                                            viewModel.addStory(textStory, "#0d9488")
                                            textStory = ""
                                            showStoryDialog = false
                                        }
                                    }) { Text("Post Story") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showStoryDialog = false }) { Text("Cancel") }
                                }
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showStoryDialog = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Post Status", fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }

                    items(storiesList) { story ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (story.isDoctor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                    .padding(3.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(story.authorAvatar, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = story.authorName,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(64.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Publish Case Button Spacer Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .clickable { showCreateDialog = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Loc.get(language, "create_post_hint"),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Active Feed List of Cases
        posts.filter { !it.isFlagged }.let { filtered ->
            items(filtered) { post ->
                PostItem(
                    post = post,
                    viewModel = viewModel,
                    onCommentClicked = { activeCommentPostId = post.id }
                )
            }
        }
    }

    // --- Create Case Dialogue Register ---
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(Loc.get(language, "post_btn"), fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = postContent,
                        onValueChange = { postContent = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        placeholder = { Text("What is the diagnostic case or inquiry context?") },
                    )

                    Text("Media / Post Type Attachment Model:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("TEXT", "IMAGE", "PDF", "POLL").forEach { type ->
                            val selected = selectedType == type
                            FilterChip(
                                selected = selected,
                                onClick = { selectedType = type },
                                label = { Text(type, fontSize = 10.sp) }
                            )
                        }
                    }

                    if (selectedType == "POLL") {
                        OutlinedTextField(
                            value = pollOpt1,
                            onValueChange = { pollOpt1 = it },
                            placeholder = { Text("Option 1") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = pollOpt2,
                            onValueChange = { pollOpt2 = it },
                            placeholder = { Text("Option 2") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (postContent.isNotBlank()) {
                            val pollOpts = if (selectedType == "POLL") listOf(pollOpt1, pollOpt2) else emptyList()
                            viewModel.createPost(postContent, selectedType, pollOpts)
                            
                            // reset
                            postContent = ""
                            pollOpt1 = ""
                            pollOpt2 = ""
                            selectedType = "TEXT"
                            showCreateDialog = false
                        }
                    }
                ) { Text("Publish") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }

    // --- Comments Sheet Dialog ---
    if (activeCommentPostId != null) {
        val targetPost = posts.find { it.id == activeCommentPostId }
        AlertDialog(
            onDismissRequest = { activeCommentPostId = null },
            title = { Text("Case Clinical Comments", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.width(360.dp).height(280.dp).verticalScroll(rememberScrollState())) {
                    targetPost?.commentsList?.forEach { comment ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(comment.authorAvatar, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(comment.authorName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("(${comment.authorRole})", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Text(comment.content, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                    if (targetPost?.commentsList?.isEmpty() == true) {
                        Text("No discussions yet. Be the first to share your opinion!", fontSize = 12.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(Loc.get(language, "comment_hint")) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            viewModel.addCommentToPost(activeCommentPostId!!, commentText)
                            commentText = ""
                            activeCommentPostId = null
                        }
                    }
                ) { Text(Loc.get(language, "comment_btn")) }
            },
            dismissButton = {
                TextButton(onClick = { activeCommentPostId = null }) { Text("Close") }
            }
        )
    }
}

@Composable
fun PostItem(post: Post, viewModel: MainViewModel, onCommentClicked: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(post.authorAvatar, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (post.isAuthorVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified clinical license badge", tint = TealPrimary, modifier = Modifier.size(14.dp))
                        }
                    }
                    Text("${post.authorRole} • ${System.currentTimeMillis() - post.timestamp}ms ago", fontSize = 10.sp, color = Color.Gray)
                }
                
                // Active badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(post.mediaType ?: "TEXT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Case Content
            Spacer(modifier = Modifier.height(12.dp))
            Text(post.content, style = MaterialTheme.typography.bodyLarge, fontSize = 13.sp)

            // Dynamic Image representation
            if (post.mediaType == "IMAGE") {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFFE2F1F1), Color(0xFFCCEEEE)))),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(32.dp), tint = TealPrimary)
                        Text(post.mediaUrl ?: "Attached Clinical Chest Scan Diagram", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                    }
                }
            }

            // Interactive Poll Box
            if (post.isPoll) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    post.pollOptions.forEachIndexed { idx, opt ->
                        val isVoted = post.votedOptionIndex != null
                        val isThisVoted = post.votedOptionIndex == idx
                        val voteCount = post.pollVotes.getOrNull(idx) ?: 0
                        
                        OutlinedButton(
                            onClick = { if (!isVoted) viewModel.submitPollVote(post.id, idx) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isThisVoted) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(opt, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                if (isVoted) {
                                    Text("$voteCount votes", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            // Media attachment representation
            if (post.mediaType == "PDF") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFECEF))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Case_Report_Cardio_Guidelines.pdf", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }

            // Actions panel: Reactions + Replies
            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reactions
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val reactionsList = listOf("Insightful", "Clinical Agree")
                    reactionsList.forEach { reaction ->
                        val count = post.reactions.getOrDefault(reaction, 0)
                        IconButton(
                            onClick = { viewModel.addReaction(post.id, reaction) },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (reaction == "Insightful") "💡" else "✅", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$count", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Replies button
                IconButton(onClick = onCommentClicked) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${post.commentsCount}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 2: AI ASSISTANT COUNSEL
// ==========================================
@Composable
fun TabAssistant(viewModel: MainViewModel, language: LanguageCode) {
    var queryText by remember { mutableStateOf("") }
    var responseOutput by remember { mutableStateOf("") }
    var isQuerying by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Face, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(Loc.get(language, "symptom_title"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = Loc.get(language, "symptom_desc"),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        OutlinedTextField(
            value = queryText,
            onValueChange = { queryText = it },
            placeholder = { Text(Loc.get(language, "symptom_placeholder")) },
            modifier = Modifier.fillMaxWidth().height(110.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = {
                if (queryText.isNotBlank()) {
                    isQuerying = true
                    scope.launch {
                        val resp = GeminiApiClient.queryGemini(
                            prompt = queryText,
                            systemPrompt = "You are a professional medical consultant. Share structured assessments."
                        )
                        responseOutput = resp
                        isQuerying = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isQuerying) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("EXAMINE SYMPTOMS & DIALOGUE", fontWeight = FontWeight.Bold)
            }
        }

        if (responseOutput.isNotBlank()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Medsync Gemini AI Verdict Analysis:", fontWeight = FontWeight.Bold, color = TealPrimary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(responseOutput, fontSize = 13.sp, lineHeight = 18.sp)
                }
            }
        }
    }
}

// ==========================================
// TAB 3: WORKSPACE CHAT CLINIC
// ==========================================
@Composable
fun TabChat(viewModel: MainViewModel, language: LanguageCode) {
    val messages by viewModel.messagesFlow.collectAsState()
    val isAiEnabled by viewModel.isChatAiEnabled.collectAsState()
    var messageText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        // Toggle Smart AI Reply auto intervention
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("AI Triage Bot inside Chat", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Instantly analyzes and responds to clinical guidelines", fontSize = 9.sp, color = Color.Gray)
                }
                Switch(checked = isAiEnabled, onCheckedChange = { viewModel.isChatAiEnabled.value = it })
            }
        }

        // Messages Box Scrollable list
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (msg.senderId == "ai_bot") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(msg.senderAvatar, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(msg.senderName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(msg.content, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
        }

        // Send Text Bar row
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Send secure medical message...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendChatMessage(messageText)
                        messageText = ""
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
            }
        }
    }
}

// ==========================================
// TAB 4: CIRCLES & COMMUNITIES
// ==========================================
@Composable
fun TabGroups(viewModel: MainViewModel, language: LanguageCode) {
    val circlesList by viewModel.groups.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Clinical Circles & Clinical Study Registers",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Join targeted spaces for direct communication, publications study, and patient verification boards.",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        items(circlesList) { circle ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(circle.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when (circle.category) {
                                        "CLINICAL" -> Color(0xFFFEE2E2)
                                        else -> Color(0xFFE2F1F1)
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                if (circle.category == "CLINICAL") "Clinician" else "Public Forum",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (circle.category == "CLINICAL") Color.Red else TealPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(circle.description, fontSize = 11.sp, lineHeight = 16.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${circle.membersCount} Members • ${circle.postsCount} cases active", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        
                        Button(
                            onClick = { viewModel.joinGroup(circle.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (circle.isJoined) Color.Gray else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (circle.isJoined) "Joined ✓" else "Join Circle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 5: ALERTS & EMERGENCY SOS
// ==========================================
@Composable
fun TabNotifications(viewModel: MainViewModel, language: LanguageCode) {
    val alerts by viewModel.notificationsFlow.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Emergency Notifications & Clinical Audit Logs", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("Real-time system alarms, AI guard alerts, and priority shift SOS calls.", fontSize = 11.sp, color = Color.Gray)
        }

        if (alerts.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No emergency triage alarms raised at this moment.", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        items(alerts) { alert ->
            val isSOS = alert.type == "SOS"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSOS) Color(0xFFFFF1F2) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, if (isSOS) Color(0xFFFECDD3) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSOS) Icons.Default.Warning else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (isSOS) Color(0xFFEF4444) else TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(alert.title, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = if (isSOS) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onBackground)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(alert.message, fontSize = 12.sp, lineHeight = 16.sp)
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (alert.isRead) "Read ✓" else "UNREAD ALARM",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (alert.isRead) Color.Gray else Color(0xFFEF4444)
                        )
                        TextButton(onClick = { viewModel.removeNotification(alert.localId) }) {
                            Text("Acknowledge", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 6: PROFILE PORTFOLIO
// ==========================================
@Composable
fun TabProfile(viewModel: MainViewModel, language: LanguageCode) {
    val current by viewModel.currentUser.collectAsState()
    var editName by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }
    var editSpecialization by remember { mutableStateOf("") }
    
    LaunchedEffect(current) {
        current?.let {
            editName = it.name
            editBio = it.bio
            editSpecialization = it.specialization
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Avatar Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(current?.avatar ?: "👨‍⚕️", fontSize = 36.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(current?.name ?: "", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (current?.isVerified == true) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TealPrimary)
                    }
                }
                Text("${current?.role} • ${current?.specialization?.ifBlank { "General clinical" }}", fontSize = 12.sp, color = Color.Gray)
                
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${current?.followersCount ?: 0}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(Loc.get(language, "fol_count"), fontSize = 11.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${current?.followingCount ?: 0}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(Loc.get(language, "following_count"), fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Editable credentials fields
        Text("Update Profile Credentials & Specialization", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        OutlinedTextField(
            value = editName,
            onValueChange = { editName = it },
            label = { Text("Clinical Profile Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = editSpecialization,
            onValueChange = { editSpecialization = it },
            label = { Text("Specialization / Bio Title") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = editBio,
            onValueChange = { editBio = it },
            label = { Text("Biography, Research & Hospital Ward Logs") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                viewModel.updateUserProfile(editName, editBio, editSpecialization, current?.avatar ?: "👨‍⚕️")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Update Clinical Profile", fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// TAB 7: ADMINISTRATIVE PORTAL (ADMIN ONLY)
// ==========================================
@Composable
fun TabAdmin(viewModel: MainViewModel, language: LanguageCode) {
    val usersList by viewModel.usersFlow.collectAsState()
    val backupMsg by viewModel.backupMessage.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(Loc.get(language, "admin_title"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(Loc.get(language, "admin_desc"), fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
        }

        // Database action
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("SQLite Cache Database Controls", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.runSystemBackup() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("MIGRATED OFFLINE CACHE ENCRYPTED BACKUP")
                    }
                    if (backupMsg != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(backupMsg!!, color = TealPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("Verify Clinical Specialist Licenses", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        items(usersList) { user ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${user.email} • ${user.role}", fontSize = 11.sp, color = Color.Gray)
                        Text(if (user.isVerified) "Verified Clinical Specialist ✓" else "Pending Medical License Verification", fontSize = 10.sp, color = if (user.isVerified) TealPrimary else Color.Red, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Row {
                        IconButton(onClick = { viewModel.toggleUserVerification(user.email) }) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = if (user.isVerified) TealPrimary else Color.Gray)
                        }
                        IconButton(onClick = { viewModel.deleteUser(user.email) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}
