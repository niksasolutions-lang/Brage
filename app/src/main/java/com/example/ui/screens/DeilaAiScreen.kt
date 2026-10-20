package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ServiceItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class DeilaMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val matchedService: ServiceItem? = null,
    val safetyTip: String? = null,
    val estimatedCostRange: String? = null,
    val timestamp: String = "Just now"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeilaAiScreen(
    allServices: List<ServiceItem>,
    onSelectService: (ServiceItem) -> Unit,
    onBookService: (ServiceItem) -> Unit,
    onCallProvider: (name: String, role: String, initials: String, contextTitle: String, phone: String) -> Unit,
    onChatProvider: (name: String, role: String, initials: String, contextTitle: String, phone: String) -> Unit,
    onOpenProfilePicView: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null
) {
    if (onBackClick != null) {
        BackHandler {
            onBackClick()
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var showDeilaProfileModal by remember { mutableStateOf(false) }

    // Chat mode state
    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var activeThinkingStep by remember { mutableStateOf(1) }

    val messages = remember {
        mutableStateListOf(
            DeilaMessage(
                id = "d1",
                text = "Hello! I am Deila, your Brage AI Service Concierge. Describe any issue or task you need done — whether it's a home repair, deep cleaning, car trouble, moving, or personal care. I will diagnose the job, estimate costs, and connect you with the highest-rated verified specialist.",
                isFromUser = false
            )
        )
    }

    val starterPrompts = listOf(
        "Kitchen sink leaking water under cabinet",
        "Move-out deep clean for 2BR apartment",
        "Mount 65-inch TV & set up mesh Wi-Fi",
        "Mobile car wash & deep interior steam",
        "Severe neck tension & sports massage",
        "IKEA wardrobe assembly & wall anchor"
    )

    fun handleSend(query: String) {
        if (query.isBlank()) return
        val userMsg = DeilaMessage(
            id = "user_${System.currentTimeMillis()}",
            text = query.trim(),
            isFromUser = true
        )
        messages.add(userMsg)
        inputText = ""

        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)
            isThinking = true
            activeThinkingStep = 1
            delay(500)
            activeThinkingStep = 2
            delay(650)
            activeThinkingStep = 3
            delay(650)
            activeThinkingStep = 4
            delay(500)
            isThinking = false

            val q = query.lowercase()
            val matched = when {
                q.contains("leak") || q.contains("sink") || q.contains("plumb") || q.contains("pipe") || q.contains("clog") ->
                    allServices.find { it.id == "repair_plumb" }
                q.contains("clean") || q.contains("move-out") || q.contains("deposit") || q.contains("apartment") ->
                    if (q.contains("move")) allServices.find { it.id == "clean_move" } else allServices.find { it.id == "clean_deep" }
                q.contains("tv") || q.contains("wifi") || q.contains("wi-fi") || q.contains("smart") || q.contains("tech") ->
                    allServices.find { it.id == "tech_smart_wifi" }
                q.contains("car") || q.contains("wash") || q.contains("detail") || q.contains("auto") ->
                    allServices.find { it.id == "auto_detail_mobile" }
                q.contains("massage") || q.contains("neck") || q.contains("back") || q.contains("relax") ->
                    allServices.find { it.id == "beauty_massage" }
                q.contains("ikea") || q.contains("assemble") || q.contains("furniture") || q.contains("wardrobe") ->
                    allServices.find { it.id == "repair_furniture" }
                q.contains("dog") || q.contains("pet") || q.contains("groom") ->
                    allServices.find { it.id == "pet_grooming_mobile" }
                q.contains("train") || q.contains("fitness") || q.contains("workout") ->
                    allServices.find { it.id == "well_personal_train" }
                q.contains("move") || q.contains("van") || q.contains("hauling") ->
                    allServices.find { it.id == "move_local_apt" }
                q.contains("math") || q.contains("tutor") || q.contains("stem") ->
                    allServices.find { it.id == "tutor_math_stem" }
                q.contains("chef") || q.contains("dinner") || q.contains("cook") ->
                    allServices.find { it.id == "event_private_chef" }
                else ->
                    allServices.find { it.id == "repair_plumb" } ?: allServices.first()
            }

            val diagnosis = when (matched?.categoryId) {
                "cat_repairs" ->
                    "Deila Brain Engine analyzed your query. This issue requires professional pipe inspection and gasket replacement by our licensed plumbing specialist."
                "cat_cleaning" ->
                    "Deila Brain Engine reviewed the scope. A comprehensive deep sanitation is recommended, covering degreasing, baseboards, and HEPA particulate filtering."
                "cat_tech" ->
                    "This matches our Smart Home & Network engineering service. The specialist can eliminate dead zones and conceal wall cabling."
                "cat_auto" ->
                    "Mobile detailing is ideal here. The mobile unit brings spot-free deionized water and interior steam sanitization directly to your driveway."
                "cat_beauty" ->
                    "A certified neuromuscular massage therapist can travel directly to your residence with an ergonomic heated table."
                else ->
                    "Deila Brain Engine matched your inquiry with our top-rated specialists certified in this service category."
            }

            val safetyTip = when (matched?.categoryId) {
                "cat_repairs" -> "DIY Precaution: Shut off the cold water isolation valve underneath the sink clockwise to prevent moisture damage until the technician arrives."
                "cat_tech" -> "Quick Tip: Keep your router admin password and ISP modem model number handy for rapid network optimization."
                "cat_cleaning" -> "Prep Tip: Pre-sort personal valuables so the cleaning team can focus on deep scrubbing countertops and appliances."
                else -> "Brage Protection: All jobs booked through Deila AI include our $1,000 Property & Satisfaction Guarantee."
            }

            val priceRange = matched?.let { "$${it.basePrice.toInt()} - $${(it.basePrice * 1.3).toInt()}" }

            val responseMsg = DeilaMessage(
                id = "deila_${System.currentTimeMillis()}",
                text = diagnosis,
                isFromUser = false,
                matchedService = matched,
                safetyTip = safetyTip,
                estimatedCostRange = priceRange
            )
            messages.add(responseMsg)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onBackClick != null) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(40.dp)
                                    .testTag("deila_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .border(
                                    2.dp,
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFF6366F1),
                                            Color(0xFFA855F7),
                                            Color(0xFF06B6D4)
                                        )
                                    ),
                                    CircleShape
                                )
                                .clickable { showDeilaProfileModal = true }
                                .testTag("deila_avatar_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_deila_avatar),
                                contentDescription = "Deila AI Profile Pic",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDeilaProfileModal = true }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Deila AI",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF6366F1).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "CONCIERGE PRO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF4F46E5),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Online • Tap avatar for AI Profile",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    // Starter Prompts Row
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(starterPrompts) { prompt ->
                            SuggestionChip(
                                onClick = { handleSend(prompt) },
                                label = { Text(prompt, fontSize = 12.sp) }
                            )
                        }
                    }

                    // Input Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Ask Deila AI about any service...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("deila_input_field"),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { handleSend(inputText) },
                            enabled = inputText.isNotBlank(),
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("deila_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (inputText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("deila_ai_screen"),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages) { msg ->
                DeilaMessageRow(
                    msg = msg,
                    onSelectService = onSelectService,
                    onBookService = onBookService,
                    onCallProvider = onCallProvider,
                    onChatProvider = onChatProvider
                )
            }

            if (isThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Deila AI is analyzing categories and estimating prices...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showDeilaProfileModal) {
        AlertDialog(
            onDismissRequest = { showDeilaProfileModal = false },
            title = null,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .border(
                                    3.dp,
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFF6366F1),
                                            Color(0xFFA855F7),
                                            Color(0xFF06B6D4)
                                        )
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_deila_avatar),
                                contentDescription = "Deila AI Portrait",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Online indicator
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                                .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Deila AI Concierge",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Autonomous Brage Service Engine v3.5",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6366F1),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                    Text("Response Latency:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("< 450 ms", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                    Text("Specialist Match Rate:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("99.4% Accuracy", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                    Text("Service Coverage:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("60+ Home & Auto Categories", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Deila AI continuously analyzes user symptoms, predicts repair parts, computes transparent labor rate estimates, and matches you with background-verified master technicians.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    if (onOpenProfilePicView != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = {
                                showDeilaProfileModal = false
                                onOpenProfilePicView()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_view_deila_pic_fullscreen"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2CF6FF).copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color(0xFF2CF6FF), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Deila AI Profile Picture Page", color = Color(0xFF2CF6FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDeilaProfileModal = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close AI Profile")
                }
            }
        )
    }
}

@Composable
fun DeilaMessageRow(
    msg: DeilaMessage,
    onSelectService: (ServiceItem) -> Unit,
    onBookService: (ServiceItem) -> Unit,
    onCallProvider: (name: String, role: String, initials: String, contextTitle: String, phone: String) -> Unit,
    onChatProvider: (name: String, role: String, initials: String, contextTitle: String, phone: String) -> Unit
) {
    if (msg.isFromUser) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = msg.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            Text(
                text = msg.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp, end = 4.dp)
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(
                            1.5.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF6366F1),
                                    Color(0xFFA855F7),
                                    Color(0xFF06B6D4)
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_deila_avatar),
                        contentDescription = "Deila AI",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = msg.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(14.dp)
                        )
                    }

                    // Safety / Preparation Tip
                    msg.safetyTip?.let { tip ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFEF3C7)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tip,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF92400E),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Matched Recommended Service Card
                    msg.matchedService?.let { service ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "RECOMMENDED SERVICE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }

                                    msg.estimatedCostRange?.let { range ->
                                        Text(
                                            text = "Est. $range",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = service.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Specialist: ${service.provider.name} (★ ${service.rating})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Book Now button
                                    Button(
                                        onClick = { onBookService(service) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Book Now", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }

                                    // Chat with Pro
                                    FilledTonalIconButton(
                                        onClick = {
                                            onChatProvider(
                                                service.provider.name,
                                                service.provider.title,
                                                service.provider.initials,
                                                service.title,
                                                "+1 (555) 382-9912"
                                            )
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = "Chat", tint = MaterialTheme.colorScheme.primary)
                                    }

                                    // Call Pro
                                    FilledTonalIconButton(
                                        onClick = {
                                            onCallProvider(
                                                service.provider.name,
                                                service.provider.title,
                                                service.provider.initials,
                                                service.title,
                                                "+1 (555) 382-9912"
                                            )
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
