package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.CommentsLightBottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ShortVideoItem(
    val id: String,
    val title: String,
    val channelHandle: String,
    val channelName: String,
    val avatarInitials: String,
    val avatarColor: Color,
    val isVerified: Boolean = true,
    val durationSeconds: Int = 60,
    val views: String = "124K",
    val initialLikes: Int = 14200,
    val commentsCount: Int = 342,
    val audioTitle: String = "Sunrise Peak Ambient Flute - Original Sound",
    val tags: String = "#NandiHills #DEILAPLAY #Shorts #NowPlaying",
    val thumbUrl: String = "https://picsum.photos/400/800?1",
    val primaryColor: Color = Color(0xFF00E5FF)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortPageScreen(
    initialShortId: String = "s1",
    onBackClick: () -> Unit,
    onOpenVideoPlay: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Intercept hardware/system back button
    BackHandler {
        onBackClick()
    }

    val coroutineScope = rememberCoroutineScope()

    val shortItems = remember {
        listOf(
            ShortVideoItem(
                id = "s1",
                title = "Sunrise Trek at Nandi Hills in 60s ⛰️ Catching the first rays above the clouds!",
                channelHandle = "@peaktrail",
                channelName = "PeakTrail",
                avatarInitials = "PT",
                avatarColor = Color(0xFF00E5FF),
                isVerified = true,
                durationSeconds = 60,
                views = "124K",
                initialLikes = 14200,
                commentsCount = 342,
                audioTitle = "PeakTrail • Sunrise Ambient Flute",
                tags = "#NandiHills #DEILAPLAY #Shorts #SunriseTrek",
                thumbUrl = "https://picsum.photos/400/800?1",
                primaryColor = Color(0xFF00E5FF)
            ),
            ShortVideoItem(
                id = "s2",
                title = "Lo-Fi Vinyl Scratch Ambience & Cyber Beats 🎧 100% Analog Tape Saturation",
                channelHandle = "@chillwave_beats",
                channelName = "Chillwave Beats",
                avatarInitials = "CB",
                avatarColor = Color(0xFF818CF8),
                isVerified = true,
                durationSeconds = 60,
                views = "98K",
                initialLikes = 8900,
                commentsCount = 186,
                audioTitle = "DEILA Sound Labs • Lo-Fi Chillhop 808",
                tags = "#LoFi #FocusMusic #DEILAPLAY #NowPlaying",
                thumbUrl = "https://picsum.photos/400/800?2",
                primaryColor = Color(0xFF818CF8)
            ),
            ShortVideoItem(
                id = "s3",
                title = "Multimeter Beep Continuity Test in 15s ⚡ Pro Electrician Diagnostics",
                channelHandle = "@marcus_chen",
                channelName = "Master Tech Marcus",
                avatarInitials = "MC",
                avatarColor = Color(0xFF38BDF8),
                isVerified = true,
                durationSeconds = 45,
                views = "210K",
                initialLikes = 28500,
                commentsCount = 512,
                audioTitle = "Workshop Telemetry Sound • Live Feed",
                tags = "#Electrician #Diagnostics #ProTips #DEILAPLAY",
                thumbUrl = "https://picsum.photos/400/800?3",
                primaryColor = Color(0xFF38BDF8)
            ),
            ShortVideoItem(
                id = "s4",
                title = "Quick PEX Crimp Plumbing Hack 🔧 Zero leaks in under 30 seconds!",
                channelHandle = "@soren_diy",
                channelName = "Soren Lindqvist",
                avatarInitials = "SL",
                avatarColor = Color(0xFF34D399),
                isVerified = false,
                durationSeconds = 30,
                views = "145K",
                initialLikes = 19100,
                commentsCount = 278,
                audioTitle = "Pro Plumbing Masterclass Audio",
                tags = "#Plumbing #DIY #Shorts #BrageTips",
                thumbUrl = "https://picsum.photos/400/800?4",
                primaryColor = Color(0xFF34D399)
            ),
            ShortVideoItem(
                id = "s5",
                title = "How I Built This Neon Synth in 24 Hours ✨ DIY Circuit Design Live Test",
                channelHandle = "@maker_gal",
                channelName = "Maker Gal",
                avatarInitials = "MG",
                avatarColor = Color(0xFFF472B6),
                isVerified = true,
                durationSeconds = 60,
                views = "312K",
                initialLikes = 45700,
                commentsCount = 892,
                audioTitle = "Neon Synth Cyber Wave • Analog Stereo",
                tags = "#DIYElectronics #NeonSynth #DEILAPLAY #Maker",
                thumbUrl = "https://picsum.photos/400/800?5",
                primaryColor = Color(0xFFF472B6)
            )
        )
    }

    val initialIndex = remember(initialShortId) {
        val found = shortItems.indexOfFirst { it.id == initialShortId }
        if (found >= 0) found else 0
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { shortItems.size }
    )

    // Global states
    var isMuted by remember { mutableStateOf(false) }
    var showCommentsSheet by remember { mutableStateOf(false) }
    var activeCommentsPostTitle by remember { mutableStateOf("DEILA PLAY • 2h") }
    var activeCommentsPostSubtitle by remember { mutableStateOf("Our new short film is live now ✨ #DEILAPLAY #NowPlaying") }
    var shareToastMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .testTag("screen_deila_shorts_page")
    ) {
        // Vertical Pager for smooth swipe between shorts
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val short = shortItems[page]
            val isCurrentPage = pagerState.currentPage == page

            ShortItemPage(
                item = short,
                isActive = isCurrentPage,
                isMuted = isMuted,
                onToggleMute = { isMuted = !isMuted },
                onOpenComments = {
                    activeCommentsPostTitle = "${short.channelName} • 2h"
                    activeCommentsPostSubtitle = short.title
                    showCommentsSheet = true
                },
                onShare = {
                    shareToastMessage = "Link for '${short.title}' copied to clipboard!"
                }
            )
        }

        // ==========================================
        // TOP OVERLAY: Back, DEILA SHORTS Title & Controls
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back button + Brand
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onBackClick() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("btn_shorts_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DEILA ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "SHORTS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00E5FF)
                )
            }

            // Top Actions: Search, Mute & Create
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = { isMuted = !isMuted },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("btn_shorts_toggle_mute")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = if (isMuted) "Unmute" else "Mute",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF212121).copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3300E5FF)),
                    modifier = Modifier
                        .clickable {
                            shareToastMessage = "Camera / Create Shorts mode initialized!"
                        }
                        .testTag("btn_shorts_create")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Short",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Create",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // ==========================================
        // SHARE / ACTION TOAST
        // ==========================================
        AnimatedVisibility(
            visible = shareToastMessage != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 56.dp)
        ) {
            shareToastMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0A4A8A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF)),
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = msg,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                LaunchedEffect(msg) {
                    delay(2500)
                    shareToastMessage = null
                }
            }
        }

        // ==========================================
        // COMMENTS LIGHT BOTTOM SHEET (User requested CommentsLightStyle)
        // ==========================================
        CommentsLightBottomSheet(
            isOpen = showCommentsSheet,
            onDismiss = { showCommentsSheet = false },
            postTitle = activeCommentsPostTitle,
            postSubtitle = activeCommentsPostSubtitle
        )
    }
}

/**
 * Individual Short Video Item with interactive playback, like heart burst, side rail, and creator metadata
 */
@Composable
private fun ShortItemPage(
    item: ShortVideoItem,
    isActive: Boolean,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    onOpenComments: () -> Unit,
    onShare: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableIntStateOf(item.initialLikes) }
    var isDisliked by remember { mutableStateOf(false) }
    var isSubscribed by remember { mutableStateOf(false) }
    var isExpandedCaption by remember { mutableStateOf(false) }
    var isBookmarked by remember { mutableStateOf(false) }

    // Double tap heart animation
    var showHeartBurst by remember { mutableStateOf(false) }

    // Playback scrubber seconds
    var progressSeconds by remember { mutableFloatStateOf(0f) }

    // Running playback loop when active
    LaunchedEffect(isActive, isPlaying) {
        if (isActive && isPlaying) {
            while (isPlaying) {
                delay(200)
                progressSeconds = (progressSeconds + 0.2f) % item.durationSeconds
            }
        }
    }

    // Disc rotation infinite animation
    val infiniteTransition = rememberInfiniteTransition(label = "disc_spin")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_angle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (!isLiked) {
                            isLiked = true
                            likesCount += 1
                        }
                        showHeartBurst = true
                    },
                    onTap = {
                        isPlaying = !isPlaying
                    }
                )
            }
            .testTag("short_container_${item.id}")
    ) {
        // 1. Background Video Simulation Texture
        AsyncImage(
            model = item.thumbUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay for high-contrast readable text
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.55f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.40f),
                        Color.Black.copy(alpha = 0.90f)
                    )
                )
            )
        }

        // Animated sound visualizer waveform bars (when playing)
        if (isPlaying && isActive) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                repeat(4) { idx ->
                    val waveHeight by infiniteTransition.animateFloat(
                        initialValue = 8f,
                        targetValue = 28f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(400 + idx * 120, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "wave_$idx"
                    )
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(waveHeight.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF00E5FF).copy(alpha = 0.8f))
                    )
                }
            }
        }

        // Center Pause Indicator (when tapped to pause)
        if (!isPlaying) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.65f),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00E5FF)),
                modifier = Modifier
                    .size(68.dp)
                    .align(Alignment.Center)
                    .shadow(16.dp, CircleShape, spotColor = Color(0xFF00E5FF))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }

        // Double-Tap Heart Burst Animation
        if (showHeartBurst) {
            Box(
                modifier = Modifier.align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFFF2D55),
                    modifier = Modifier.size(100.dp)
                )
            }
            LaunchedEffect(showHeartBurst) {
                delay(700)
                showHeartBurst = false
            }
        }

        // ==========================================
        // 2. RIGHT-SIDE ACTION RAIL (Like, Dislike, Comments, Share, Remix, Vinyl)
        // ==========================================
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 12.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        val nextLiked = !isLiked
                        isLiked = nextLiked
                        likesCount = if (nextLiked) likesCount + 1 else (likesCount - 1).coerceAtLeast(0)
                        if (nextLiked) isDisliked = false
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFF1E1E1E).copy(alpha = 0.6f), CircleShape)
                        .testTag("btn_short_like_${item.id}")
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Like",
                        tint = if (isLiked) Color(0xFF00E5FF) else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = formatCount(likesCount),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Dislike Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        isDisliked = !isDisliked
                        if (isDisliked && isLiked) {
                            isLiked = false
                            likesCount = (likesCount - 1).coerceAtLeast(0)
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFF1E1E1E).copy(alpha = 0.6f), CircleShape)
                        .testTag("btn_short_dislike_${item.id}")
                ) {
                    Icon(
                        imageVector = if (isDisliked) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                        contentDescription = "Dislike",
                        tint = if (isDisliked) Color(0xFFFF453A) else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "Dislike",
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Comments Button -> Opens CommentsLightStyle Sheet
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onOpenComments,
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFF1E1E1E).copy(alpha = 0.6f), CircleShape)
                        .testTag("btn_short_comments_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "${item.commentsCount}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Share Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFF1E1E1E).copy(alpha = 0.6f), CircleShape)
                        .testTag("btn_short_share_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = "Share",
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Bookmark / Save
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { isBookmarked = !isBookmarked },
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFF1E1E1E).copy(alpha = 0.6f), CircleShape)
                        .testTag("btn_short_bookmark_${item.id}")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) Color(0xFF00E5FF) else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = if (isBookmarked) "Saved" else "Remix",
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Spinning Audio Vinyl Record
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .rotate(if (isPlaying && isActive) discRotation else 0f)
                    .clip(CircleShape)
                    .background(Color(0xFF111827))
                    .border(2.dp, Color(0xFF00E5FF), CircleShape)
                    .testTag("short_audio_disc_${item.id}"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E5FF))
                )
            }
        }

        // ==========================================
        // 3. BOTTOM OVERLAY: Creator, Caption, Audio Ticker
        // ==========================================
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .navigationBarsPadding()
                .padding(start = 14.dp, bottom = 24.dp)
        ) {
            // Creator Row: Avatar + Handle + Subscribe Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Avatar badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(item.avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.avatarInitials,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A2540)
                    )
                }

                Text(
                    text = item.channelHandle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (item.isVerified) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF00E5FF),
                        modifier = Modifier.size(14.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Verified",
                                tint = Color.Black,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }

                // Subscribe Button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSubscribed) Color(0xFF27272A) else Color.White,
                    modifier = Modifier
                        .clickable { isSubscribed = !isSubscribed }
                        .testTag("btn_short_subscribe_${item.id}")
                ) {
                    Text(
                        text = if (isSubscribed) "Screwed" else "Screw",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSubscribed) Color.White else Color.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Caption Text
            Text(
                text = item.title,
                fontSize = 13.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                maxLines = if (isExpandedCaption) 5 else 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
                modifier = Modifier
                    .clickable { isExpandedCaption = !isExpandedCaption }
                    .testTag("short_caption_${item.id}")
            )

            // Tags
            Text(
                text = item.tags,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF00E5FF),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Audio Track Marquee
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.audioTitle,
                    fontSize = 11.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // ==========================================
        // 4. PROGRESS TIMELINE BAR (Bottom Edge)
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(3.dp)
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            val fraction = (progressSeconds / item.durationSeconds.toFloat()).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(Color(0xFF00E5FF))
            )
        }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> "$count"
    }
}
