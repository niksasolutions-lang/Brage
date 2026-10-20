package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

data class DiagnosticPin(
    val xRatio: Float,
    val yRatio: Float,
    val label: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCallScreen(
    contactName: String,
    contactRole: String,
    contactInitials: String,
    contextTitle: String,
    phoneNumber: String,
    onEndCall: () -> Unit,
    onSwitchToAudioCall: () -> Unit = {},
    onBookService: () -> Unit = {}
) {
    BackHandler {
        onEndCall()
    }

    var callDurationSeconds by remember { mutableStateOf(0) }
    var isConnected by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var isCameraOff by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(true) }
    var isTorchOn by remember { mutableStateOf(false) }
    var isArDiagnosticsActive by remember { mutableStateOf(true) }
    var showInCallChat by remember { mutableStateOf(false) }
    var showPostCallSummary by remember { mutableStateOf(false) }
    var snapshotTakenNotification by remember { mutableStateOf<String?>(null) }

    // Diagnostic Pins on screen
    val diagnosticPins = remember {
        mutableStateListOf(
            DiagnosticPin(0.52f, 0.46f, "Suspected Gasket Seal Wear", Color(0xFFEF4444)),
            DiagnosticPin(0.38f, 0.68f, "P-Trap Joint Alignment", Color(0xFFF59E0B))
        )
    }

    // In-call quick messages
    val inCallMessages = remember {
        mutableStateListOf(
            "Technician: Video stream connected in 1080p HD.",
            "Technician: Point camera 6 inches closer to the valve junction."
        )
    }
    var chatInputText by remember { mutableStateOf("") }

    // Draggable PiP offset
    var pipOffsetX by remember { mutableFloatStateOf(0f) }
    var pipOffsetY by remember { mutableFloatStateOf(0f) }

    // Connect call after 1.8 seconds
    LaunchedEffect(Unit) {
        delay(1800)
        isConnected = true
        while (true) {
            delay(1000)
            callDurationSeconds++
        }
    }

    // Snapshot notification dismissal
    LaunchedEffect(snapshotTakenNotification) {
        if (snapshotTakenNotification != null) {
            delay(2500)
            snapshotTakenNotification = null
        }
    }

    // Audio Waveform pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "audioWave")
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w1"
    )
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(550, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label = "w2"
    )
    val wave3 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w3"
    )

    val minutes = callDurationSeconds / 60
    val seconds = callDurationSeconds % 60
    val formattedDuration = String.format("%02d:%02d", minutes, seconds)

    val isDeilaContact = contactName.contains("Deila", ignoreCase = true) || contactRole.contains("AI", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("video_call_screen")
    ) {
        // --- 1. FULL SCREEN REMOTE VIDEO FEED (Technician / Specialist view) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (isArDiagnosticsActive) {
                            val ratioX = offset.x / size.width
                            val ratioY = offset.y / size.height
                            diagnosticPins.add(
                                DiagnosticPin(
                                    xRatio = ratioX,
                                    yRatio = ratioY,
                                    label = "Inspection Marker #${diagnosticPins.size + 1}",
                                    color = Color(0xFF06B6D4)
                                )
                            )
                        }
                    }
                }
        ) {
            if (isDeilaContact) {
                // Deila AI Luminous Hologram Video Feed
                Image(
                    painter = painterResource(id = R.drawable.img_deila_avatar),
                    contentDescription = "Deila AI Video Stream",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Holographic Tech Mesh Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF1E1B4B).copy(alpha = 0.5f),
                                    Color.Transparent,
                                    Color(0xFF0F172A).copy(alpha = 0.85f)
                                )
                            )
                        )
                )
            } else {
                // Specialist Camera View Background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF1E293B),
                                    Color(0xFF0F172A),
                                    Color(0xFF050811)
                                )
                            )
                        )
                )

                // Simulated Live Technician Video feed with Hero Banner texture
                Image(
                    painter = painterResource(id = R.drawable.brage_hero_banner_1791385066842),
                    contentDescription = "Specialist Video Stream",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.65f)
                )

                // Scanline and lighting vignette
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF0F172A).copy(alpha = 0.7f),
                                    Color.Transparent,
                                    Color(0xFF0F172A).copy(alpha = 0.85f)
                                )
                            )
                        )
                )
            }

            // --- AR Diagnostic Overlay Markers on Video ---
            if (isArDiagnosticsActive) {
                diagnosticPins.forEach { pin ->
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val posX = (maxWidth * pin.xRatio) - 40.dp
                        val posY = (maxHeight * pin.yRatio) - 40.dp

                        Box(
                            modifier = Modifier
                                .offset(x = posX, y = posY)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F172A).copy(alpha = 0.85f))
                                .border(1.5.dp, pin.color, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(pin.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = pin.label,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 2. TOP HUD: CALL METRICS, QUALITY, AR TOGGLE ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Specialist Info Capsule
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDeilaContact) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_deila_avatar),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = contactInitials,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = contactName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(13.dp))
                            }
                            Text(
                                text = if (isConnected) formattedDuration else "Connecting HD Stream...",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isConnected) Color(0xFF10B981) else Color(0xFFFBBF24),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Stream Quality & Diagnostic Snapshot buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // AR Diagnostic Pen Toggle
                    FilledTonalIconButton(
                        onClick = { isArDiagnosticsActive = !isArDiagnosticsActive },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (isArDiagnosticsActive) Color(0xFF6366F1) else Color(0xFF1E293B).copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Draw,
                            contentDescription = "AR Annotation",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Snapshot photo tool
                    FilledTonalIconButton(
                        onClick = {
                            snapshotTakenNotification = "📸 Diagnostic snapshot captured & attached to job sheet"
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Color(0xFF1E293B).copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Take Snapshot",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // In-Call Chat overlay button
                    FilledTonalIconButton(
                        onClick = { showInCallChat = !showInCallChat },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (showInCallChat) Color(0xFF0284C7) else Color(0xFF1E293B).copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "In-Call Messages",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Connection Quality Pill
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.75f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF10B981)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("1080p 60fps • 22ms HD", color = Color(0xFFE2E8F0), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Audio waveform feedback
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.75f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(modifier = Modifier.width(3.dp).height((12 * wave1).dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF38BDF8)))
                        Box(modifier = Modifier.width(3.dp).height((16 * wave2).dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF34D399)))
                        Box(modifier = Modifier.width(3.dp).height((10 * wave3).dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF38BDF8)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isMuted) "Mic Muted" else "HD Audio", color = Color.White, fontSize = 10.sp)
                    }
                }
            }

            // Snapshot Alert Banner
            snapshotTakenNotification?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8))
                ) {
                    Text(
                        text = msg,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // --- 3. DRAGGABLE LOCAL USER CAMERA PiP WINDOW ---
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 100.dp, end = 16.dp)
                .offset { IntOffset(pipOffsetX.roundToInt(), pipOffsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        pipOffsetX += dragAmount.x
                        pipOffsetY += dragAmount.y
                    }
                }
                .width(110.dp)
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E293B))
                .border(2.dp, if (isFrontCamera) Color(0xFF38BDF8) else Color(0xFF10B981), RoundedCornerShape(16.dp))
        ) {
            if (isCameraOff) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.VideocamOff, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Camera Off", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                    }
                }
            } else {
                // Simulated Camera Feed (Front / Rear)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isFrontCamera) Brush.verticalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                            else Brush.verticalGradient(listOf(Color(0xFF0F766E), Color(0xFF115E59)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFrontCamera) Icons.Default.Face else Icons.Default.HomeRepairService,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    )

                    // Local Camera label pill
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isFrontCamera) "You (Front)" else "Inspection (Rear)",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick Flip Camera overlay icon on PiP
            IconButton(
                onClick = { isFrontCamera = !isFrontCamera },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FlipCameraIos,
                    contentDescription = "Flip Camera",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // --- 4. IN-CALL MESSAGES OVERLAY SHEET ---
        if (showInCallChat) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.9f)
                    .height(280.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.95f)),
                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Video Call Messages",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { showInCallChat = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        inCallMessages.forEach { msg ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B)
                            ) {
                                Text(
                                    text = msg,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = chatInputText,
                            onValueChange = { chatInputText = it },
                            placeholder = { Text("Type message to specialist...", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        FilledIconButton(
                            onClick = {
                                if (chatInputText.isNotBlank()) {
                                    inCallMessages.add("You: $chatInputText")
                                    chatInputText = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // --- 5. BOTTOM CONTROL DOCK ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Quick Action Pills Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Torch Toggle (useful for inspection)
                FilterChip(
                    selected = isTorchOn,
                    onClick = { isTorchOn = !isTorchOn },
                    label = { Text("Inspection Light", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFF59E0B),
                        selectedLabelColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Switch to Voice-Only call
                FilterChip(
                    selected = false,
                    onClick = onSwitchToAudioCall,
                    label = { Text("Voice Only", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Media Controls Dock
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.9f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute / Unmute Mic
                    VideoCallControlButton(
                        icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        isActive = isMuted,
                        activeColor = Color(0xFFEF4444),
                        onClick = { isMuted = !isMuted },
                        label = if (isMuted) "Unmute" else "Mute"
                    )

                    // Video Camera On/Off
                    VideoCallControlButton(
                        icon = if (isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        isActive = isCameraOff,
                        activeColor = Color(0xFFEF4444),
                        onClick = { isCameraOff = !isCameraOff },
                        label = if (isCameraOff) "Start Video" else "Stop Video"
                    )

                    // Flip Camera (Front / Rear)
                    VideoCallControlButton(
                        icon = Icons.Default.FlipCameraAndroid,
                        isActive = !isFrontCamera,
                        activeColor = Color(0xFF10B981),
                        onClick = { isFrontCamera = !isFrontCamera },
                        label = "Flip"
                    )

                    // Red Hang Up Button
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDC2626))
                            .clickable { showPostCallSummary = true }
                            .testTag("btn_end_video_call"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }

    // --- 6. POST-VIDEO CALL DIAGNOSTIC SUMMARY DIALOG ---
    if (showPostCallSummary) {
        AlertDialog(
            onDismissRequest = onEndCall,
            title = null,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentTurnedIn,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Video Diagnostic Session Complete",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Call Duration: $formattedDuration with $contactName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Specialist Live Notes:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "✓ Visual inspection verified ${diagnosticPins.size} diagnostic points\n✓ Recommended Service: Immediate P-trap assembly reseal\n✓ Estimated Work Time: 45 minutes",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quote Estimate:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("$75 - $95 (Parts Included)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFF059669))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEndCall()
                        onBookService()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Book Specialist Now ($75)")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onEndCall,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close & Save to Chat")
                }
            }
        )
    }
}

@Composable
fun VideoCallControlButton(
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor else Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
