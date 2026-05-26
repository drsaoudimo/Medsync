package com.example.presentation.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AppointmentEntity
import com.example.data.db.ChatMessageEntity
import com.example.data.db.MedicationEntity
import com.example.presentation.MainViewModel
import com.example.presentation.AdminUser
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import com.example.presentation.chat.ChatViewModel
import com.example.utils.Language
import com.example.utils.Loc
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    chatViewModel: ChatViewModel
) {
    val language by viewModel.currentLanguage.collectAsState()
    val userRole by viewModel.selectedRole.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val userProfile by viewModel.currentUser.collectAsState()

    // Determine layout direction (RTL if Arabic)
    val layoutDirection = if (language == Language.AR) LayoutDirection.Rtl else LayoutDirection.Ltr

    // Active bottom navigation index
    var activeTab by remember { mutableStateOf(0) }
    
    val isUserAdmin = userProfile?.role == "Admin" || userRole == "Admin" || userProfile?.email == "smohamed.stf@gmail.com"

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Loc.t("app_name", language),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        // Quick Toggle indicator matching top stats bar from Design
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (syncStatus == "Synced") Color(0xFF22C55E) else Color(0xFFF59E0B))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (syncStatus == "Synced") Loc.t("offline_mode", language) else "Syncing...",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        label = { Text("Home", fontWeight = FontWeight.SemiBold, fontSize = 9.sp) },
                        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") }
                    )
                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        label = { Text("AI Chat", fontWeight = FontWeight.SemiBold, fontSize = 9.sp) },
                        icon = { Icon(imageVector = Icons.Default.ChatBubble, contentDescription = "AI Assistant") }
                    )
                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        label = { Text("Prescript", fontWeight = FontWeight.SemiBold, fontSize = 9.sp) },
                        icon = { Icon(imageVector = Icons.Default.Assignment, contentDescription = "Digital Prescriptions") }
                    )
                    NavigationBarItem(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        label = { Text("SOS", fontWeight = FontWeight.SemiBold, fontSize = 9.sp) },
                        icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = "Emergency SOS") }
                    )
                    NavigationBarItem(
                        selected = activeTab == 4,
                        onClick = { activeTab = 4 },
                        label = { Text("Settings", fontWeight = FontWeight.SemiBold, fontSize = 9.sp) },
                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") }
                    )
                    if (isUserAdmin) {
                        NavigationBarItem(
                            selected = activeTab == 5,
                            onClick = { activeTab = 5 },
                            label = { Text("Admin", fontWeight = FontWeight.Bold, fontSize = 9.sp) },
                            icon = { Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Admin") }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF8FAFC))
            ) {
                Crossfade(targetState = activeTab, label = "tab_switcher") { tabIndex ->
                    when (tabIndex) {
                        0 -> MainHomeTab(viewModel, language, userRole, userProfile)
                        1 -> AIChatTab(chatViewModel, language)
                        2 -> PrescriptionsTab(viewModel, language, userRole)
                        3 -> SOSTab(language)
                        4 -> SettingsTab(viewModel, language)
                        5 -> AdminDashboardTab(viewModel, language)
                        else -> MainHomeTab(viewModel, language, userRole, userProfile)
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 1: MAIN MEDICAL CLINICAL CLINIC HOME
// ==========================================
@Composable
fun MainHomeTab(
    viewModel: MainViewModel,
    lang: Language,
    role: String,
    profile: com.example.data.db.UserEntity?
) {
    val appointments by viewModel.appointments.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header Banner
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile?.fullName?.take(2)?.uppercase() ?: "MD",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${Loc.t("welcome", lang)},",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = profile?.fullName ?: "Dr. Sarah Ahmed",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (role == "Doctor") "${profile?.specialty} • Card 402" else Loc.t(role.lowercase(), lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Professional Polish: AI Insights Banner (Interactive and beautiful)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF005FB0), Color(0xFF003C6E))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Loc.t("ai_assistant", lang).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = Loc.t("ai_insight_bp", lang),
                        fontSize = 14.sp,
                        color = Color.White,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Live Dynamic Analytics Chart (Draw on Canvas simulation for cardiac metrics)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Loc.t("analytics_summary", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Vitals Baseline Monitoring (Real-time Heart Rate Variability)",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Canvas chart
                    CardiacChart()
                }
            }
        }

        // General Multi-Role Interactive Widgets
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Widget 1
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = Loc.t("stats_appointments", lang),
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${appointments.size}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Loc.t("new_alert", lang).take(7),
                                fontSize = 9.sp,
                                color = Color(0xFF22C55E),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
                // Widget 2
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = Loc.t("stats_pending", lang),
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "04",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Loc.t("due_soon", lang).uppercase(),
                                fontSize = 8.sp,
                                color = Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Subtitle: Today's Appointments with dynamic adding mechanics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Loc.t("today_appointments", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
                Button(
                    onClick = {
                        // Quick demo add
                        viewModel.addAppointment(
                            "Patricia Durand",
                            "04:15 PM",
                            "In-Person",
                            "Emergency follow up"
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Demo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (appointments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = Loc.t("empty_appointments", lang),
                                color = Color(0xFF64748B),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            items(appointments) { appt ->
                AppointmentCard(appt, lang) {
                    viewModel.deleteAppointment(appt)
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(
    appt: AppointmentEntity,
    lang: Language,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hour Indicator
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (appt.type == "Telehealth") Color(0xFFEFF6FF) else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = appt.dateTimeStr.substringBefore(" "),
                        fontWeight = FontWeight.Bold,
                        color = if (appt.type == "Telehealth") Color(0xFF005FB0) else Color(0xFF475569),
                        fontSize = 15.sp
                    )
                    Text(
                        text = appt.dateTimeStr.substringAfter(" ", "PM"),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Patient details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appt.patientName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = appt.reason,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (appt.type == "Telehealth") Color(0xFFEFF6FF) else Color(0xFFF0FDF4))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (appt.type == "Telehealth") Loc.t("telehealth", lang) else Loc.t("in_person", lang),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (appt.type == "Telehealth") Color(0xFF005FB0) else Color(0xFF15803D)
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
fun CardiacChart() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(vertical = 4.dp)
    ) {
        val width = size.width
        val height = size.height
        val gridLines = 5
        val pointsCount = 40
        
        // Draw grid
        for (i in 0..gridLines) {
            val y = (height / gridLines) * i
            drawLine(
                color = Color(0xFFE2E8F0),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        // Draw animated/simulated ECG heartbeat path
        val path = Path()
        path.moveTo(0f, height * 0.5f)
        
        for (i in 0..pointsCount) {
            val x = (width / pointsCount) * i
            // Every 8 points draw an R-spike representation
            val y = if (i % 8 == 0) {
                height * 0.2f
            } else if (i % 8 == 1) {
                height * 0.8f
            } else if (i % 8 == 2) {
                height * 0.45f
            } else {
                height * (0.5f + (Math.sin(i.toDouble() * 1.5).toFloat() * 0.08f))
            }
            path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = Color(0xFF005FB0),
            style = Stroke(
                width = 5f,
                cap = StrokeCap.Round
            )
        )
    }
}

// ==========================================
// TAB 2: MODERN CHAT BOT INTERFACE
// ==========================================
@Composable
fun AIChatTab(
    chatViewModel: ChatViewModel,
    lang: Language
) {
    val messages by chatViewModel.chatHistory.collectAsState(initial = emptyList())
    val isSending by chatViewModel.isSending.collectAsState()
    var textInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Option Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Loc.t("ai_assistant", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF0F172A)
            )
            TextButton(onClick = { chatViewModel.clearHistory() }) {
                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(Loc.t("clear_chat", lang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Suggestion Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf("Check Symptoms", "Drug Interaction", "Cardiac BP Advice")
            suggestions.forEach { label ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .clickable {
                            chatViewModel.sendMessage(label)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chats lists
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
            }
            if (isSending) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
            }
        }

        // Bottom text field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text(Loc.t("chat_placeholder", lang), fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (textInput.trim().isNotEmpty()) {
                        chatViewModel.sendMessage(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = Loc.t("send", lang),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessageEntity) {
    val isUser = msg.sender == "user"
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp
                        )
                    )
                    .background(
                        if (isUser) MaterialTheme.colorScheme.primary else Color.White
                    )
                    .border(
                        1.dp,
                        if (isUser) Color.Transparent else Color(0xFFE2E8F0),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(14.dp)
            ) {
                Text(
                    text = msg.text,
                    fontSize = 14.sp,
                    color = if (isUser) Color.White else Color(0xFF1E293B),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// ==========================================
// TAB 3: PRESCRIPTIONS & PHARMACY MANAGEMENT
// ==========================================
@Composable
fun PrescriptionsTab(
    viewModel: MainViewModel,
    lang: Language,
    role: String
) {
    val meds by viewModel.medications.collectAsState(initial = emptyList())
    var dialogOpen by remember { mutableStateOf(false) }
    var barcodedScanOpen by remember { mutableStateOf(false) }

    // Prescription Handpad sig
    val signaturePoints = remember { mutableStateListOf<Offset>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Loc.t("prescription_mgr", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = Loc.t("drug_interaction_warning", lang),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        }

        // Signature Handpad (Only accessible if Doctor/Staff)
        if (role == "Doctor" || role == "Nurse") {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = Loc.t("signature_pad", lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                            TextButton(onClick = { signaturePoints.clear() }) {
                                Text(Loc.t("clear_signature", lang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        signaturePoints.add(change.position)
                                    }
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                for (i in 0 until signaturePoints.size - 1) {
                                    drawLine(
                                        color = Color(0xFF0F172A),
                                        start = signaturePoints[i],
                                        end = signaturePoints[i+1],
                                        strokeWidth = 6f,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Subtitle Inventory
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Loc.t("pharmacy_inventory", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A)
                )
                Button(
                    onClick = { barcodedScanOpen = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) {
                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Loc.t("barcode_scan", lang), fontSize = 11.sp)
                }
            }
        }

        items(meds) { med ->
            MedicationCard(med, lang) {
                viewModel.updateMedicationStock(med.id, med.stockQty + 50)
            }
        }
    }

    if (barcodedScanOpen) {
        AlertDialog(
            onDismissRequest = { barcodedScanOpen = false },
            title = { Text("Simulating Barcode Scanner") },
            text = { Text("Medsync detected product label UPC: 88019902035 - Lisinopril 10mg. Auto linking complete with local records.") },
            confirmButton = {
                TextButton(onClick = { barcodedScanOpen = false }) { Text("OK") }
            }
        )
    }
}

@Composable
fun MedicationCard(med: MedicationEntity, lang: Language, onReplenish: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = med.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "${Loc.t("category", lang)}: ${med.category}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
                Text(
                    text = "$${med.price}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "${Loc.t("stock", lang)}: ${med.stockQty}",
                        fontSize = 12.sp,
                        color = if (med.stockQty < 30) Color(0xFFEF4444) else Color(0xFF475569),
                        fontWeight = if (med.stockQty < 30) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = "Dosage limit: ${med.dosage}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
                if (med.stockQty < 30) {
                    Button(
                        onClick = onReplenish,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Replenish Stock", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 4: ADVANCED SOS MEDICAL DISPATCH
// ==========================================
@Composable
fun SOSTab(lang: Language) {
    var sosSent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(160.dp)
                .clickable { sosSent = true },
            shape = CircleShape,
            color = Color(0xFFFFEBF0),
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = Color(0xFFFECDD3)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(88.dp),
                            shape = CircleShape,
                            color = Color(0xFFE11D48)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = Loc.t("sos_button", lang),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color(0xFFBE123C)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = Loc.t("sos_pulsing", lang),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = Color(0xFF64748B)
        )
        if (sosSent) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFEF2F2))
                    .padding(16.dp)
            ) {
                Text(
                    text = Loc.t("sos_sent", lang),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFBE123C),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// ==========================================
// TAB 5: LANGUAGE SETTINGS OPTIONS SYNC
// ==========================================
@Composable
fun SettingsTab(viewModel: MainViewModel, lang: Language) {
    val currentUser by viewModel.currentUser.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val adminPassword by viewModel.adminPassword.collectAsState()

    var oldPassInput by remember { mutableStateOf("") }
    var newPassInput by remember { mutableStateOf("") }
    var changeMessage by remember { mutableStateOf<String?>(null) }
    var changeSuccessful by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = Loc.t("settings_title", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF0F172A)
            )
        }

        // Languages choices
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Loc.t("language_toggle", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Language.values().forEach { l ->
                            val selected = l == lang
                            Button(
                                onClick = { viewModel.setLanguage(l) },
                                modifier = Modifier.weight(1f),
                                colors = if (selected) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                            ) {
                                Text(text = l.code, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Active Roles switcher
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Loc.t("role_select", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val roles = listOf("Doctor", "Pharmacist", "Patient")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        roles.forEach { r ->
                            val selected = viewModel.selectedRole.collectAsState().value == r
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
                                    .clickable { viewModel.setRole(r) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selected,
                                    onClick = { viewModel.setRole(r) }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = Loc.t(r.lowercase(), lang),
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ACCOUNT PASSWORD SECURITY PANEL (اعدادات المستخدم)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == Language.AR) "إعدادات أمان كلمة المرور" else "Account Security settings",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                    
                    Text(
                        text = "User / مستخدم: ${currentUser?.email ?: "smohamed.stf@gmail.com"}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = oldPassInput,
                        onValueChange = { oldPassInput = it; changeMessage = null },
                        label = { Text(Loc.t("current_passwd", lang)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newPassInput,
                        onValueChange = { newPassInput = it; changeMessage = null },
                        label = { Text(Loc.t("new_passwd", lang)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    changeMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = msg,
                            color = if (changeSuccessful) Color(0xFF22C55E) else Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (oldPassInput.isEmpty() || newPassInput.isEmpty()) {
                                changeSuccessful = false
                                changeMessage = "Please enter older and newer password / الحقول مطلوبة"
                            } else if (oldPassInput != adminPassword) {
                                changeSuccessful = false
                                changeMessage = "Error: Current password is incorrect / كلمة المرور الحالية غير صحيحة"
                            } else {
                                viewModel.changeAdminPassword(newPassInput)
                                changeSuccessful = true
                                changeMessage = "Password updated successfully! / تم تغيير رمز الأمان بنجاح"
                                oldPassInput = ""
                                newPassInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = Loc.t("save_passwd", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Action Buttons
        item {
            Button(
                onClick = { /* Force synchronize background worker mockup */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.CloudSync, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(Loc.t("sync_now", lang), fontWeight = FontWeight.Bold)
            }
        }

        item {
            OutlinedButton(
                onClick = { viewModel.simulateLogout() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(Loc.t("logout", lang), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// TAB 6: ENTERPRISE-GRADE ADMIN PANEL
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardTab(viewModel: MainViewModel, lang: Language) {
    val usersList by viewModel.adminUsersList.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var broadcastText by remember { mutableStateOf("") }
    var showBroadcastSuccess by remember { mutableStateOf(false) }
    var broadcastMsg by remember { mutableStateOf("") }
    
    // Backup and Maintenance simulated states
    var maintenanceMode by remember { mutableStateOf(false) }
    var aiDiagnosticLimit by remember { mutableStateOf(0.75f) }
    var isBackupRunning by remember { mutableStateOf(false) }
    var backupCompletedMsg by remember { mutableStateOf<String?>(null) }
    
    // Add User dialog states
    var showAddUserDialog by remember { mutableStateOf(false) }
    var newUserName by remember { mutableStateOf("") }
    var newUserEmail by remember { mutableStateOf("") }
    var newUserPhone by remember { mutableStateOf("") }
    var newUserRole by remember { mutableStateOf("Patient") }
    var newUserInfo by remember { mutableStateOf("") }
    var addUserError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Welcome Header & Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (lang == Language.AR) "لوحة القيادة والمراقبة" else "Platform Operations & Metrics",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF0F172A),
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = if (lang == Language.AR) "تحليلات الأمان وإدارة الصلاحيات السريرية" else "Clinical security & system telemetry controls",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Operational Telemetry Cards Row (RTL-ready grid)
        item {
            val totalCount = usersList.size
            val patientCount = usersList.count { it.role == "Patient" }
            val clinicalStaff = usersList.count { it.role != "Patient" && it.role != "Admin" }
            val unverifiedDocsCount = usersList.count { it.role == "Doctor" && !it.isVerified }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Registered Cardinal Stats
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (lang == Language.AR) "المستخدمين" else "Total Users",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalCount",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = if (lang == Language.AR) "نشط على الخوادم" else "Synced cloud profiles",
                            fontSize = 9.sp,
                            color = Color(0xFF22C55E),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Patients Under Watch stats
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (lang == Language.AR) "المرضى المراقبين" else "Active Patients",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$patientCount",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0284C7)
                        )
                        Text(
                            text = if (lang == Language.AR) "مراقبة مستمرة" else "Room & DB storage",
                            fontSize = 9.sp,
                            color = Color(0xFF0284C7)
                        )
                    }
                }

                // Clinical approval queues
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (lang == Language.AR) "غير المؤهلين" else "Unverified Docs",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$unverifiedDocsCount",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = if (unverifiedDocsCount > 0) Color(0xFFEF4444) else Color(0xFF22C55E)
                        )
                        Text(
                            text = if (lang == Language.AR) "انتظار التحقق" else "Waiting clearance",
                            fontSize = 9.sp,
                            color = if (unverifiedDocsCount > 0) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Live Analytics Chart Section (Custom Compose Canvas)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == Language.AR) "مخطط زيارات ومعدل تشخيص الذكاء الاصطناعي" else "Diagnostic Flow & Cloud Sync Load",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEFF6FF))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "Live Telemetry",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3B82F6)
                            )
                        }
                    }
                    
                    Text(
                        text = if (lang == Language.AR) "تمثيل طوبولوجي للمتغيرات السريرية بالأيام" else "Persistence barcode metrics of semantic clinical flow (Mon-Sun)",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Canvas Chart representing a beautiful line chart
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .padding(top = 8.dp)
                    ) {
                        val points = listOf(20f, 50f, 15f, 85f, 40f, 65f, 110f)
                        val maxPoints = 120f
                        val widthBetween = size.width / (points.size - 1)
                        val heightRatio = size.height / maxPoints

                        // Draw Grid lines
                        for (i in 1..4) {
                            val gridY = size.height - (i * (size.height / 5))
                            drawLine(
                                color = Color(0xFFF1F5F9),
                                start = Offset(0f, gridY),
                                end = Offset(size.width, gridY),
                                strokeWidth = 1f
                            )
                        }

                        // Path calculation for the wavy analytics line
                        val path = Path()
                        path.moveTo(0f, size.height - (points[0] * heightRatio))
                        for (i in 1 until points.size) {
                            val x = i * widthBetween
                            val y = size.height - (points[i] * heightRatio)
                            
                            // bezier curve control points calculations
                            val prevX = (i - 1) * widthBetween
                            val prevY = size.height - (points[i - 1] * heightRatio)
                            path.cubicTo(
                                (prevX + x) / 2, prevY,
                                (prevX + x) / 2, y,
                                x, y
                            )
                        }

                        // Draw background glowing area
                        val fillPath = Path()
                        fillPath.addPath(path)
                        fillPath.lineTo(size.width, size.height)
                        fillPath.lineTo(0f, size.height)
                        fillPath.close()

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF3B82F6).copy(alpha = 0.25f),
                                    Color.White
                                )
                            )
                        )

                        // Draw Main Line
                        drawPath(
                            path = path,
                            color = Color(0xFF3B82F6),
                            style = Stroke(width = 5f, cap = StrokeCap.Round)
                        )

                        // Draw Dots at vertex positions
                        for (i in points.indices) {
                            val x = i * widthBetween
                            val y = size.height - (points[i] * heightRatio)
                            drawCircle(
                                color = Color(0xFF1D4ED8),
                                radius = 7f,
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 4f,
                                center = Offset(x, y)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Days Indicator labels matching canvas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                            Text(day, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }
        }

        // Instant Broadcast General Alerts Controller
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color(0xFFE11D48),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == Language.AR) "بث تنبيه طبي عاجل للأجهزة" else "Global Broadcast Dispatcher",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Text(
                        text = if (lang == Language.AR) "يرسل إشعار فوري لجميع الكوادر الطبية والمرضى" else "Simulates Firebase Cloud Messaging (FCM) push notification directly to live sockets:",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = broadcastText,
                        onValueChange = { broadcastText = it },
                        placeholder = { Text(if (lang == Language.AR) "أدخل نص التنبيه العاجل..." else "Enter broadcast message content...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (showBroadcastSuccess) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF0FDF4))
                                .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "SUCCESS: Broadcast sent successfully to clinical sockets!\nMessage: \"$broadcastMsg\"",
                                color = Color(0xFF15803D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Button(
                        onClick = {
                            if (broadcastText.isNotBlank()) {
                                broadcastMsg = broadcastText
                                showBroadcastSuccess = true
                                broadcastText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (lang == Language.AR) "إرسال التنبيه الآن" else "Dispatch Alert to Firebase FCM",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Clinical / System Configuration panel
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (lang == Language.AR) "إعدادات تشغيل النظام" else "Platform Tuning & Maintenance",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Maintenance mode Toggle switches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (lang == Language.AR) "وضع الصيانة الطبي" else "Lockout / Maintenance Mode",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = if (lang == Language.AR) "يقيد دخول المرضى غير الحالات الطارئة" else "Blocks non-clinical accounts",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Switch(
                            checked = maintenanceMode,
                            onCheckedChange = { maintenanceMode = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // AI tuning variables slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (lang == Language.AR) "الحد الأقصى لتشخيص AI" else "AI Diagnostics Weight Ratio",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "${(aiDiagnosticLimit * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = aiDiagnosticLimit,
                            onValueChange = { aiDiagnosticLimit = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (backupCompletedMsg != null) {
                        Text(
                            text = backupCompletedMsg!!,
                            color = Color(0xFF16A34A),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            isBackupRunning = true
                            coroutineScope.launch {
                                delay(2000)
                                isBackupRunning = false
                                backupCompletedMsg = "Backup created! SQL Room base & Firebase metadata synced safely at ${System.currentTimeMillis()}"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isBackupRunning) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (lang == Language.AR) "أخذ نسخة احتياطية سريرية كاملة" else "Backup Base Database Now")
                        }
                    }
                }
            }
        }

        // Active Users & Clinic Staff Directory
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (lang == Language.AR) "إدارة الأدوار والمستخدمين" else "User Management Controls",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = if (lang == Language.AR) "تجميد، ترقية وتوثيق تراخيص الدكاترة" else "Suspend profiles, edit clearance, verify status",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
                
                // Add target user button
                IconButton(
                    onClick = { showAddUserDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add User",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Search Filter Directory
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (lang == Language.AR) "ابحث بالاسم، الإيميل أو الدور الطبي..." else "Filter directory by name, email, role...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Render sorted search targets
        val filteredUsers = usersList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.email.contains(searchQuery, ignoreCase = true) ||
            it.role.contains(searchQuery, ignoreCase = true)
        }

        if (filteredUsers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (lang == Language.AR) "لا يوجد تطابق لخيارات البحث!" else "No search matches found.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(filteredUsers) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, if (user.status == "Suspended") Color(0xFFFECDD3) else Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Avatar Icon representing role
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (user.role) {
                                            "Admin" -> Color(0xFFFEE2E2)
                                            "Doctor" -> Color(0xFFE0F2FE)
                                            "Pharmacist" -> Color(0xFFF3E8FF)
                                            "Nurse" -> Color(0xFFECFDF5)
                                            else -> Color(0xFFF1F5F9)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.name.take(1),
                                    fontWeight = FontWeight.Bold,
                                    color = when (user.role) {
                                        "Admin" -> Color(0xFF991B1B)
                                        "Doctor" -> Color(0xFF0369A1)
                                        "Pharmacist" -> Color(0xFF6B21A8)
                                        "Nurse" -> Color(0xFF065F46)
                                        else -> Color(0xFF475569)
                                    },
                                    fontSize = 15.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    if (user.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Verified Licensed Badge",
                                            tint = Color(0xFF22C55E),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${user.email} • ${user.phoneNumber}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                if (user.info.isNotBlank()) {
                                    Text(
                                        text = user.info,
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }

                            // Dynamic chip matching current active role status
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (user.status == "Suspended") Color(0xFFFEF2F2) else Color(0xFFF1F5F9)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (user.status == "Suspended") "SUSPENDED" else user.role,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (user.status == "Suspended") Color(0xFFEF4444) else Color(0xFF475569)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Controls Actions for management
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Toggle Suspend state
                            val isSuspended = user.status == "Suspended"
                            OutlinedButton(
                                onClick = { viewModel.suspendUser(user.email) },
                                modifier = Modifier.weight(1.3f).height(34.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSuspended) Color(0xFFFEE2E2) else Color.White
                                ),
                                border = BorderStroke(1.dp, if (isSuspended) Color(0xFFFCA5A5) else Color(0xFFCBD5E1))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isSuspended) Icons.Default.LockOpen else Icons.Default.Block,
                                        contentDescription = null,
                                        tint = if (isSuspended) Color(0xFFB91C1C) else Color(0xFF475569),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isSuspended) {
                                            if (lang == Language.AR) "تنشيط" else "Activate"
                                        } else {
                                            if (lang == Language.AR) "تجميد" else "Suspend"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSuspended) Color(0xFFB91C1C) else Color(0xFF475569)
                                    )
                                }
                            }

                            // 2. Doctor License verify trigger
                            if (user.role == "Doctor") {
                                OutlinedButton(
                                    onClick = { viewModel.verifyDoctor(user.email) },
                                    modifier = Modifier.weight(1.5f).height(34.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = if (user.isVerified) Color(0xFF16A34A) else Color(0xFF2563EB),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (user.isVerified) {
                                                if (lang == Language.AR) "سحب الترخيص" else "Revoke Lic"
                                            } else {
                                                if (lang == Language.AR) "توثيق الطبيب" else "Approve Doc"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (user.isVerified) Color(0xFF16A34A) else Color(0xFF2563EB)
                                        )
                                    }
                                }
                            }

                            // 3. User Role Cycler / Switcher
                            val rolesPool = listOf("Doctor", "Pharmacist", "Nurse", "Patient", "Receptionist", "Admin")
                            OutlinedButton(
                                onClick = {
                                    val currentIdx = rolesPool.indexOf(user.role).coerceAtLeast(0)
                                    val nextIdx = (currentIdx + 1) % rolesPool.size
                                    viewModel.updateUserRole(user.email, rolesPool[nextIdx])
                                },
                                modifier = Modifier.weight(1.1f).height(34.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = if (lang == Language.AR) "دور ↻" else "Role ↻",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }

                            // 4. Clean Delete user profiles
                            IconButton(
                                onClick = { viewModel.deleteUser(user.email) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFEF2F2))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Profile",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add User Dialogue overlay
    if (showAddUserDialog) {
        AlertDialog(
            onDismissRequest = { showAddUserDialog = false },
            title = {
                Text(
                    text = if (lang == Language.AR) "إضافة مستخدم أو كادر طبي" else "Add Medical Staff or Patient",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newUserName,
                        onValueChange = { newUserName = it; addUserError = null },
                        label = { Text("Full Name / الإسم الكامل") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newUserEmail,
                        onValueChange = { newUserEmail = it; addUserError = null },
                        label = { Text("Email address / الإيميل الإلكتروني") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newUserPhone,
                        onValueChange = { newUserPhone = it; addUserError = null },
                        label = { Text("Phone / رقم الموبايل") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newUserInfo,
                        onValueChange = { newUserInfo = it },
                        label = { Text("Specialty or Info (e.g. Ward B Surgeon)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Role cyclic choices switcher
                    Text("Select Medical Role:", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    val rolesList = listOf("Doctor", "Nurse", "Pharmacist", "Receptionist", "Patient", "Admin")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rolesList.forEach { r ->
                            val selected = newUserRole == r
                            FilterChip(
                                selected = selected,
                                onClick = { newUserRole = r },
                                label = { Text(r, fontSize = 10.sp) }
                            )
                        }
                    }

                    addUserError?.let {
                        Text(it, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newUserName.isBlank() || newUserEmail.isBlank() || newUserPhone.isBlank()) {
                            addUserError = "Please fill in Name, Email and Phone details!"
                        } else {
                            val targetUser = AdminUser(
                                email = newUserEmail,
                                name = newUserName,
                                role = newUserRole,
                                phoneNumber = newUserPhone,
                                info = newUserInfo,
                                isVerified = (newUserRole == "Doctor" || newUserRole == "Admin")
                            )
                            viewModel.addAdminUser(targetUser)
                            showAddUserDialog = false
                            
                            // reset
                            newUserName = ""
                            newUserEmail = ""
                            newUserPhone = ""
                            newUserInfo = ""
                            newUserRole = "Patient"
                        }
                    }
                ) {
                    Text("Save / حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUserDialog = false }) {
                    Text("Cancel / إلغاء")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

