package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CommentsLightBottomSheet
import com.example.ui.components.CommentsLightStyleContent
import com.example.ui.components.CommentsLoadingDarkNeon
import com.example.ui.components.DeilaPlayIcon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class VideoChapter(
    val title: String,
    val timestampSeconds: Int,
    val timestampFormatted: String,
    val summary: String
)

data class VideoComment(
    val id: String,
    val author: String,
    val roleBadge: String? = null,
    val text: String,
    val timeAgo: String,
    val likes: Int,
    val isLiked: Boolean = false
)

data class RelatedVideoItem(
    val id: String,
    val title: String,
    val creator: String,
    val views: String,
    val duration: String,
    val durationSeconds: Int,
    val category: String,
    val badge: String = "4K"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayScreen(
    videoId: String,
    videoTitle: String,
    videoCategory: String,
    videoDuration: String,
    creatorName: String,
    initialTimestampSeconds: Int = 0,
    isDownloading: Boolean,
    onTriggerDownload: () -> Unit,
    onBackClick: () -> Unit,
    onOpenDeilaAiChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBackClick()
    }

    val coroutineScope = rememberCoroutineScope()

    // Video Playback States
    var currentVideoId by remember { mutableStateOf(videoId) }
    var currentTitle by remember { mutableStateOf(videoTitle) }
    var currentCategory by remember { mutableStateOf(videoCategory) }
    var currentCreator by remember { mutableStateOf(creatorName) }

    // Parse duration seconds e.g. "14:20" -> 860
    val totalSeconds = remember(currentVideoId) {
        val parts = videoDuration.split(":")
        if (parts.size == 2) {
            (parts[0].toIntOrNull() ?: 14) * 60 + (parts[1].toIntOrNull() ?: 20)
        } else {
            860
        }
    }

    var currentSeconds by remember { mutableFloatStateOf(initialTimestampSeconds.toFloat().coerceAtLeast(0f)) }
    var isPlaying by remember { mutableStateOf(true) }
    var controlsVisible by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var selectedQuality by remember { mutableStateOf("1080p60 HDR") }
    var isCcEnabled by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(false) }

    // Engagement States
    var isLiked by remember { mutableStateOf(false) }
    var isDisliked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf(3240) }
    var isSavedToPlaylist by remember { mutableStateOf(false) }
    var isSubscribed by remember { mutableStateOf(false) }
    var downloadCompleted by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showShareNotification by remember { mutableStateOf(false) }
    var showAiAnswerSheet by remember { mutableStateOf(false) }
    var activeAiQuestion by remember { mutableStateOf("") }
    var activeAiAnswer by remember { mutableStateOf("") }

    // Tab state (0: Chapters & AI Copilot, 1: Discussion/Chat, 2: Up Next)
    var activeTab by remember { mutableIntStateOf(0) }

    // New Comment Input
    var newCommentText by remember { mutableStateOf("") }
    var showCommentsLightSheet by remember { mutableStateOf(false) }
    var commentsStyleMode by remember { mutableIntStateOf(0) } // 0: Standard, 1: Light Style (CommentsLightStyle), 2: Dark Neon Shimmer (CommentsLoadingDarkNeon)

    // Chapters
    val chapters = remember(currentVideoId) {
        listOf(
            VideoChapter("Introduction & High-Voltage Lockout Protocol", 0, "00:00", "OSHA lockout-tagout checklist and capacitance discharge procedure."),
            VideoChapter("Chassis Teardown & Stator Inspection", 160, "02:40", "Inspection of copper windings, insulation varnish, and bearing races."),
            VideoChapter("500V Megger Insulation Resistance Test", 315, "05:15", "Live dielectric telemetry reading 2.4 Giga-ohms to ground."),
            VideoChapter("Rewinding Phase Leads & Heat Shrink", 510, "08:30", "Soldering high-temperature silicone leads with marine heat-shrink."),
            VideoChapter("Dynamic Balancing & High-RPM Benchmark", 705, "11:45", "Vibration analysis test under 12,000 RPM continuous load.")
        )
    }

    // Comments List
    var commentsList by remember {
        mutableStateOf(
            listOf(
                VideoComment(
                    id = "c1",
                    author = "Elena Vance, PE",
                    roleBadge = "Certified Electrical Engineer",
                    text = "The Megger reading at 05:15 is text-book perfect. Appreciate Marcus pointing out the temperature coefficient on cold copper windings!",
                    timeAgo = "18m ago",
                    likes = 42,
                    isLiked = true
                ),
                VideoComment(
                    id = "c2",
                    author = "Dave Martinez",
                    roleBadge = "Level 2 EV Installer",
                    text = "Used this exact technique yesterday on an 80A commercial pedestal. The Deila AI telemetry overlay makes following along so easy.",
                    timeAgo = "45m ago",
                    likes = 19
                ),
                VideoComment(
                    id = "c3",
                    author = "Sarah Jenkins",
                    roleBadge = "Apprentice Pro",
                    text = "What torque wrench brand was used on the stator mount bolts? Looked like digital torque with angle measurement.",
                    timeAgo = "1h ago",
                    likes = 7
                )
            )
        )
    }

    // Up Next List
    val relatedVideos = remember {
        listOf(
            RelatedVideoItem(
                id = "media_2",
                title = "Sub-Zero HVAC Repair: Step-by-Step Compressor Purge",
                creator = "Master Tech Marcus Chen",
                views = "18.3K views",
                duration = "22:15",
                durationSeconds = 1335,
                category = "Home DIY Masterclass",
                badge = "POPULAR"
            ),
            RelatedVideoItem(
                id = "media_4",
                title = "Emergency Plumbing Leak Stopper & PEX Crimp Guide",
                creator = "Soren Lindqvist",
                views = "31.9K views",
                duration = "08:45",
                durationSeconds = 525,
                category = "Home DIY Masterclass",
                badge = "ESSENTIAL"
            ),
            RelatedVideoItem(
                id = "media_3",
                title = "Deep Focus Workshop: Soldering Station Ambient Soundscape",
                creator = "Deila Sound Labs",
                views = "42.1K views",
                duration = "45:00",
                durationSeconds = 2700,
                category = "Soundscapes",
                badge = "HI-RES AUDIO"
            )
        )
    }

    // Simulated Timer loop when playing
    LaunchedEffect(isPlaying, playbackSpeed) {
        while (isPlaying) {
            delay((1000 / playbackSpeed).toLong())
            if (currentSeconds < totalSeconds) {
                currentSeconds += 1f
            } else {
                isPlaying = false
                currentSeconds = totalSeconds.toFloat()
            }
        }
    }

    // Auto-hide controls after 4.5 seconds of playing
    LaunchedEffect(controlsVisible, isPlaying) {
        if (controlsVisible && isPlaying) {
            delay(4500)
            controlsVisible = false
        }
    }

    // Monitor download state
    LaunchedEffect(isDownloading) {
        if (!isDownloading && downloadCompleted) {
            // keep completed
        }
    }

    // Active chapter calculation
    val currentChapter = remember(currentSeconds, chapters) {
        chapters.lastOrNull { currentSeconds.toInt() >= it.timestampSeconds } ?: chapters.first()
    }

    // Current Simulated Subtitle/Caption
    val currentCaption = remember(currentSeconds) {
        val s = currentSeconds.toInt()
        when {
            s < 30 -> "Deila AI Video Stream: Certified Master Technician demonstration."
            s in 30..80 -> "Marcus: First, always confirm voltage isolation with a calibrated multimeter."
            s in 81..160 -> "Marcus: Zero voltage confirmed. Now removing the hex bolts securing the stator endbell."
            s in 161..240 -> "Marcus: Observe the copper windings. Notice the lack of scorching or dielectric breakdown."
            s in 241..315 -> "Marcus: We'll now hook up our high-voltage insulation tester to check phase-to-ground integrity."
            s in 316..420 -> "Marcus: Applying 500 Volts DC. Reading shows 2.4 Giga-ohms, safely beyond minimum threshold."
            s in 421..510 -> "Marcus: Prepping the new high-temp silicone phase leads with marine heat-shrink sleeves."
            s in 511..650 -> "Marcus: Ensuring full solder penetration into the terminal lugs."
            s in 651..705 -> "Marcus: Torquing all mount bolts to exactly 28 Newton-meters in cross pattern."
            else -> "Marcus: Performing final dynamic balancing run. Telemetry readings are 100% nominal!"
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("video_play_screen"),
        snackbarHost = {
            if (showShareNotification) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = Color(0xFF0F172A),
                    contentColor = Color(0xFF00E5FF),
                    action = {
                        TextButton(onClick = { showShareNotification = false }) {
                            Text("OK", color = Color(0xFF00E5FF))
                        }
                    }
                ) {
                    Text("Direct stream link & timestamp copied to clipboard!")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ==========================================
            // 1. VIDEO VIEWPORT / CINEMATIC PLAYER AREA
            // ==========================================
            val playerHeight = if (isFullscreen) 360.dp else 230.dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(playerHeight)
                    .background(Color(0xFF030811))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        controlsVisible = !controlsVisible
                    }
                    .testTag("video_player_surface")
            ) {
                // Background visualizer canvas with futuristic high-tech waves
                val infiniteTransition = rememberInfiniteTransition(label = "viz_anim")
                val waveOffset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(4000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "wave_offset"
                )
                val pulseGlow by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_glow"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Subtle background gradient mesh
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFF030712),
                                Color(0xFF0A192F),
                                Color(0xFF020617)
                            )
                        )
                    )

                    if (isPlaying) {
                        // Draw simulated dynamic audio/spectrum waves
                        val path = Path()
                        path.moveTo(0f, h * 0.72f)
                        val numPoints = 20
                        val step = w / numPoints
                        for (i in 0..numPoints) {
                            val x = i * step
                            val sinFactor = kotlin.math.sin(Math.toRadians((x * 0.4 + waveOffset).toDouble())).toFloat()
                            val y = h * 0.72f + sinFactor * 24f
                            path.lineTo(x, y)
                        }
                        path.lineTo(w, h)
                        path.lineTo(0f, h)
                        path.close()

                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color(0xFF00E5FF).copy(alpha = 0.18f * pulseGlow),
                                    Color(0xFF0A4A8A).copy(alpha = 0.05f)
                                )
                            )
                        )

                        // Center grid laser lines
                        for (x in 0..8) {
                            val lineX = w * (x / 8f)
                            drawLine(
                                color = Color(0xFF00E5FF).copy(alpha = 0.06f),
                                start = Offset(lineX, 0f),
                                end = Offset(lineX, h),
                                strokeWidth = 1f
                            )
                        }
                    }
                }

                // Center Watermark / Branding Badge
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DeilaPlayIcon(
                        isBusy = isPlaying,
                        isSelected = true,
                        size = 38.dp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "DEILA PLAY HD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color.White.copy(alpha = 0.35f)
                    )
                }

                // Closed Captions / Subtitles bar
                if (isCcEnabled && isPlaying) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = if (controlsVisible) 48.dp else 12.dp)
                            .background(Color.Black.copy(alpha = 0.78f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                            .widthIn(max = 340.dp)
                    ) {
                        Text(
                            text = currentCaption,
                            fontSize = 11.sp,
                            color = Color(0xFF2CF6FF),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // ==========================================
                // OVERLAY CONTROLS (Fade in / out on tap)
                // ==========================================
                androidx.compose.animation.AnimatedVisibility(
                    visible = controlsVisible,
                    enter = fadeIn(animationSpec = tween(150)),
                    exit = fadeOut(animationSpec = tween(200)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.52f))
                    ) {
                        // Top Bar in Video Overlay
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .align(Alignment.TopCenter),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                IconButton(
                                    onClick = onBackClick,
                                    modifier = Modifier.testTag("video_player_back_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                                Text(
                                    text = currentTitle,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // CC Toggle
                                IconButton(
                                    onClick = { isCcEnabled = !isCcEnabled },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCcEnabled) Icons.Default.ClosedCaption else Icons.Default.ClosedCaptionOff,
                                        contentDescription = "Subtitles",
                                        tint = if (isCcEnabled) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Cast to screen
                                IconButton(
                                    onClick = { showShareNotification = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cast,
                                        contentDescription = "Cast stream",
                                        tint = Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                // Settings / Resolution modal
                                IconButton(
                                    onClick = { showSettingsSheet = true },
                                    modifier = Modifier.size(36.dp).testTag("btn_player_settings")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Playback Settings",
                                        tint = Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }

                        // Center Playback Transport Controls (-10s, Play/Pause, +10s)
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(28.dp)
                        ) {
                            // -10s
                            IconButton(
                                onClick = {
                                    currentSeconds = (currentSeconds - 10f).coerceAtLeast(0f)
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("btn_rewind_10s")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay10,
                                    contentDescription = "Rewind 10 seconds",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            // Big Play / Pause Button with Cyan Glow
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF0A4A8A).copy(alpha = 0.85f),
                                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00E5FF)),
                                modifier = Modifier
                                    .size(62.dp)
                                    .shadow(12.dp, CircleShape, spotColor = Color(0xFF00E5FF))
                                    .clickable { isPlaying = !isPlaying }
                                    .testTag("btn_player_play_pause")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }

                            // +10s
                            IconButton(
                                onClick = {
                                    currentSeconds = (currentSeconds + 10f).coerceAtMost(totalSeconds.toFloat())
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("btn_forward_10s")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forward10,
                                    contentDescription = "Forward 10 seconds",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        // Bottom Controls Row: Slider, Timestamp, Speed & Fullscreen
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            // Draggable Seek Slider
                            Slider(
                                value = currentSeconds,
                                onValueChange = {
                                    currentSeconds = it
                                },
                                valueRange = 0f..totalSeconds.toFloat(),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF00E5FF),
                                    activeTrackColor = Color(0xFF00E5FF),
                                    inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp)
                                    .testTag("video_timeline_slider")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Time Display
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formatSeconds(currentSeconds.toInt()),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = " / " + formatSeconds(totalSeconds),
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // Quality Badge
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = Color(0xFF00E5FF).copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = selectedQuality.split(" ")[0],
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00E5FF),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                // Speed, Mute & Fullscreen Actions
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Speed chip
                                    Text(
                                        text = "${playbackSpeed}x",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                playbackSpeed = when (playbackSpeed) {
                                                    0.75f -> 1.0f
                                                    1.0f -> 1.25f
                                                    1.25f -> 1.5f
                                                    1.5f -> 2.0f
                                                    else -> 0.75f
                                                }
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )

                                    // Mute toggle
                                    IconButton(
                                        onClick = { isMuted = !isMuted },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                            contentDescription = "Mute Toggle",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Fullscreen toggle
                                    IconButton(
                                        onClick = { isFullscreen = !isFullscreen },
                                        modifier = Modifier.size(32.dp).testTag("btn_fullscreen_toggle")
                                    ) {
                                        Icon(
                                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                            contentDescription = "Fullscreen",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 2. VIDEO METADATA & ENGAGEMENT SECTION
            // ==========================================
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("video_details_scrollable")
            ) {
                // Title and Stats
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = currentTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "24.8K views",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "•",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Streamed 2 hrs ago",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = currentCategory,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Creator Row with Follow Button
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0B192C))
                                    .border(1.5.dp, Color(0xFF00E5FF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "MC",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF00E5FF),
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentCreator,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Master Pro",
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Text(
                                    text = "Certified Master Tech • 18.2K Followers",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { isSubscribed = !isSubscribed },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSubscribed) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF0A4A8A)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("btn_subscribe_creator")
                        ) {
                        Text(
                            text = if (isSubscribed) "Screwed" else "Screw",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSubscribed) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                        )
                        }
                    }
                }

                // Interactive Action Pills (Like, Download, Share, Save, Ask AI)
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Like Button
                        item {
                            FilterChip(
                                selected = isLiked,
                                onClick = {
                                    if (isLiked) {
                                        isLiked = false
                                        likeCount -= 1
                                    } else {
                                        isLiked = true
                                        likeCount += 1
                                        isDisliked = false
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                        contentDescription = "Like",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text("$likeCount", fontSize = 12.sp) },
                                modifier = Modifier.testTag("chip_like_video")
                            )
                        }

                        // Download Offline
                        item {
                            FilterChip(
                                selected = isDownloading || downloadCompleted,
                                onClick = {
                                    if (!isDownloading) {
                                        onTriggerDownload()
                                        downloadCompleted = true
                                    }
                                },
                                leadingIcon = {
                                    if (isDownloading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = Color(0xFF00E5FF)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = if (downloadCompleted) Icons.Default.CheckCircle else Icons.Outlined.Download,
                                            contentDescription = "Download Offline",
                                            modifier = Modifier.size(16.dp),
                                            tint = if (downloadCompleted) Color(0xFF00E5FF) else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = when {
                                            isDownloading -> "Downloading..."
                                            downloadCompleted -> "Downloaded"
                                            else -> "Download 4K"
                                        },
                                        fontSize = 12.sp
                                    )
                                },
                                modifier = Modifier.testTag("chip_download_video")
                            )
                        }

                        // Share
                        item {
                            FilterChip(
                                selected = false,
                                onClick = { showShareNotification = true },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                                },
                                label = { Text("Share", fontSize = 12.sp) }
                            )
                        }

                        // Save to Playlist
                        item {
                            FilterChip(
                                selected = isSavedToPlaylist,
                                onClick = { isSavedToPlaylist = !isSavedToPlaylist },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isSavedToPlaylist) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = "Save",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(if (isSavedToPlaylist) "Saved" else "Save", fontSize = 12.sp) }
                            )
                        }

                        // Ask AI
                        item {
                            FilterChip(
                                selected = false,
                                onClick = onOpenDeilaAiChat,
                                leadingIcon = {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = "Deila AI", tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                                },
                                label = { Text("Ask Deila AI", fontSize = 12.sp, color = Color(0xFF0A4A8A), fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }

                // Currently Playing Chapter Pill
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E5FF))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Current Chapter (${currentChapter.timestampFormatted})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0A4A8A)
                                )
                                Text(
                                    text = currentChapter.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "Jump",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Tab Header Navigation (Chapters & AI Copilot, Comments, Up Next)
                item {
                    PrimaryTabRow(
                        selectedTabIndex = activeTab,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            text = { Text("AI Copilot & Chapters", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = { Text("Comments (${commentsList.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = activeTab == 2,
                            onClick = { activeTab = 2 },
                            text = { Text("Up Next", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }

                // ==========================================
                // TAB 0: CHAPTERS & AI VIDEO COPILOT
                // ==========================================
                if (activeTab == 0) {
                    // Quick AI Questions Chips
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Instant Deila AI Video Analysis",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            val questions = listOf(
                                "What torque specs were used for the stator?" to "Marcus torqued the stator mount hex bolts to exactly 28 Nm using a cross-pattern sequence to ensure zero warpage.",
                                "Is 500V Megger test safe for 240V circuits?" to "Yes! Standard NEMA & NFPA 70E electrical standards require 500V DC Megger testing on 240V equipment to ensure at least 1.0 MΩ insulation resistance. Here it read 2.4 GΩ.",
                                "Generate required tools list" to "Required Tools:\n• Fluke 1587 FC Insulation Multimeter\n• 1/4\" Drive 8mm & 10mm Hex sockets\n• Marine-grade adhesive heat shrink\n• 40W Soldering station & 60/40 rosin-core solder"
                            )

                            questions.forEach { (q, a) ->
                                OutlinedCard(
                                    onClick = {
                                        activeAiQuestion = q
                                        activeAiAnswer = a
                                        showAiAnswerSheet = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = q,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Interactive Chapters Timeline
                    item {
                        Text(
                            text = "Interactive Timestamps",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    items(chapters) { ch ->
                        val isCurrent = currentChapter.timestampSeconds == ch.timestampSeconds
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable {
                                    currentSeconds = ch.timestampSeconds.toFloat()
                                    isPlaying = true
                                }
                                .testTag("chapter_item_${ch.timestampSeconds}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) Color(0xFF00E5FF).copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF)) else null,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isCurrent) Color(0xFF00E5FF) else Color(0xFF0A4A8A).copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = ch.timestampFormatted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) Color(0xFF030811) else Color(0xFF0A4A8A),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ch.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = ch.summary,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Playing",
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // TAB 1: COMMENTS & PRO DISCUSSION
                // ==========================================
                if (activeTab == 1) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            // Style Switcher Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilterChip(
                                        selected = commentsStyleMode == 0,
                                        onClick = { commentsStyleMode = 0 },
                                        label = { Text("Pro View", fontSize = 11.sp) },
                                        modifier = Modifier.testTag("chip_comments_pro_view")
                                    )
                                    FilterChip(
                                        selected = commentsStyleMode == 1,
                                        onClick = { commentsStyleMode = 1 },
                                        label = { Text("Light Style", fontSize = 11.sp) },
                                        modifier = Modifier.testTag("chip_comments_light_style")
                                    )
                                    FilterChip(
                                        selected = commentsStyleMode == 2,
                                        onClick = { commentsStyleMode = 2 },
                                        label = { Text("Dark Neon Shimmer", fontSize = 11.sp) },
                                        modifier = Modifier.testTag("chip_comments_dark_neon")
                                    )
                                }
                            }
                        }
                    }

                    if (commentsStyleMode == 1) {
                        item {
                            CommentsLightStyleContent(
                                onClose = { commentsStyleMode = 0 },
                                postTitle = currentTitle,
                                postSubtitle = "$currentCreator • $currentCategory",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(480.dp)
                                    .padding(horizontal = 12.dp)
                            )
                        }
                    } else if (commentsStyleMode == 2) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(460.dp)
                                    .padding(horizontal = 12.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                CommentsLoadingDarkNeon(
                                    modifier = Modifier.fillMaxSize(),
                                    commentsCountText = "Loading 12 comments..."
                                )
                            }
                        }
                    } else {
                        item {
                            // Add comment row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newCommentText,
                                    onValueChange = { newCommentText = it },
                                    placeholder = { Text("Ask a question or share pro insight...", fontSize = 12.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_new_comment"),
                                    shape = RoundedCornerShape(20.dp),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        if (newCommentText.isNotBlank()) {
                                            commentsList = listOf(
                                                VideoComment(
                                                    id = "c_${System.currentTimeMillis()}",
                                                    author = "You",
                                                    roleBadge = "Community Member",
                                                    text = newCommentText,
                                                    timeAgo = "Just now",
                                                    likes = 0
                                                )
                                            ) + commentsList
                                            newCommentText = ""
                                        }
                                    },
                                    enabled = newCommentText.isNotBlank(),
                                    modifier = Modifier.testTag("btn_post_comment")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Post comment",
                                        tint = if (newCommentText.isNotBlank()) Color(0xFF0A4A8A) else Color.Gray
                                    )
                                }
                            }
                        }

                        items(commentsList) { comment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = comment.author,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        comment.roleBadge?.let { badge ->
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(3.dp),
                                                color = Color(0xFF0A4A8A).copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = badge,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0A4A8A),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = comment.timeAgo,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = comment.text,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            commentsList = commentsList.map {
                                                if (it.id == comment.id) {
                                                    val newLiked = !it.isLiked
                                                    val newCount = if (newLiked) it.likes + 1 else it.likes - 1
                                                    it.copy(isLiked = newLiked, likes = newCount)
                                                } else it
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (comment.isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                            contentDescription = "Like comment",
                                            tint = if (comment.isLiked) Color(0xFF00E5FF) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${comment.likes}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "Reply",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable { /* reply */ }
                                    )
                                }
                            }
                        }
                    }
                }
                }

                // ==========================================
                // TAB 2: UP NEXT QUEUE
                // ==========================================
                if (activeTab == 2) {
                    items(relatedVideos) { related ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clickable {
                                    currentVideoId = related.id
                                    currentTitle = related.title
                                    currentCategory = related.category
                                    currentCreator = related.creator
                                    currentSeconds = 0f
                                    isPlaying = true
                                }
                                .testTag("up_next_item_${related.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp, 48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF050D1A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    DeilaPlayIcon(
                                        isBusy = false,
                                        isSelected = false,
                                        size = 20.dp
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = Color.Black.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(3.dp)
                                    ) {
                                        Text(
                                            text = related.duration,
                                            fontSize = 8.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = related.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${related.creator} • ${related.views}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }

    // ==========================================
    // 3. SETTINGS & QUALITY BOTTOM SHEET
    // ==========================================
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Playback & Stream Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Resolution & Quality",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                val qualityOptions = listOf("4K UHD (2160p 60fps)", "1080p60 HDR", "720p HD", "Data Saver (480p)")
                qualityOptions.forEach { q ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedQuality = q
                                showSettingsSheet = false
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = q, fontSize = 14.sp)
                        if (selectedQuality == q) {
                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color(0xFF00E5FF))
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Text(
                    text = "Playback Speed",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                val speedOptions = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    items(speedOptions) { sp ->
                        FilterChip(
                            selected = playbackSpeed == sp,
                            onClick = {
                                playbackSpeed = sp
                                showSettingsSheet = false
                            },
                            label = { Text("${sp}x") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // ==========================================
    // 4. DEILA AI ANSWER SHEET
    // ==========================================
    if (showAiAnswerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAiAnswerSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Deila AI Smart Answer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Q: $activeAiQuestion",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0A4A8A)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = activeAiAnswer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp),
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        showAiAnswerSheet = false
                        onOpenDeilaAiChat()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A4A8A))
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ask Deila AI Follow-up Questions")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Comments Light Bottom Sheet
    CommentsLightBottomSheet(
        isOpen = showCommentsLightSheet,
        onDismiss = { showCommentsLightSheet = false },
        postTitle = currentTitle,
        postSubtitle = "$currentCreator • $currentCategory"
    )
}

// Utility function to format seconds into mm:ss
private fun formatSeconds(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
