package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.Playlist
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ThreadReply(
    val id: Long,
    val user: String,
    val avatar: String,
    val time: String,
    val text: String,
    val likes: Int,
    val isLiked: Boolean = false,
    val isAuthor: Boolean = false
)

data class ThreadComment(
    val id: Long,
    val user: String,
    val avatar: String,
    val time: String,
    val text: String,
    val likes: Int,
    val isLiked: Boolean = false,
    val replies: List<ThreadReply> = emptyList()
)

data class ReplyingToState(
    val threadId: Long,
    val userName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeilaPlayScreen(
    videoId: String = "synth_neon",
    videoTitle: String = "How I Built This Neon Synth in 24 Hours | DIY Electronics",
    channelInfo: String = "1.2M subs • 87K views • 2 days ago",
    playlists: List<Playlist> = emptyList(),
    onSaveToPlaylist: ((Long, String) -> Unit)? = null,
    onCreatePlaylist: ((String) -> Unit)? = null,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Hide bottom bar handled by MainAppContent when on DeilaPlayScreen
    // Intercept back button
    BackHandler {
        onBackClick()
    }

    val coroutineScope = rememberCoroutineScope()

    // 2. Fake loading timer (1800ms) matching DeilaPlayScreen.js
    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1800)
        loading = false
    }

    // Video Player state
    var isPlaying by remember { mutableStateOf(false) }
    var currentSeconds by remember { mutableIntStateOf(42) }
    val totalSeconds = 210
    var isMuted by remember { mutableStateOf(false) }
    var isLikedVideo by remember { mutableStateOf(false) }
    var videoLikesCount by remember { mutableIntStateOf(1420) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            if (currentSeconds < totalSeconds) {
                currentSeconds++
            } else {
                isPlaying = false
            }
        }
    }

    // Threaded Comments State
    var textInput by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<ReplyingToState?>(null) }
    var expandedThreads by remember { mutableStateOf<Set<Long>>(setOf(1L, 2L)) } // Default expanded for rich view

    // Playlist dialog state
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var showCreatePlaylistInput by remember { mutableStateOf(false) }

    var comments by remember {
        mutableStateOf(
            listOf(
                ThreadComment(
                    id = 1L,
                    user = "Sarah Kim",
                    avatar = "SK",
                    time = "2 days ago",
                    text = "This tutorial is exactly what I needed! The way they explained the threading logic finally clicked for me. Thanks!",
                    likes = 142,
                    replies = listOf(
                        ThreadReply(
                            id = 11L,
                            user = "Dev_Mike",
                            avatar = "DM",
                            time = "1 day ago",
                            text = "Totally agree! I was stuck on the indentation part for hours. Adding the recursive check solved it.",
                            likes = 27
                        ),
                        ThreadReply(
                            id = 12L,
                            user = "Sarah Kim",
                            avatar = "SK",
                            time = "1 day ago",
                            text = "@Dev_Mike yep that recursive check was the key! Glad it helped 🙌",
                            likes = 8,
                            isAuthor = true
                        )
                    )
                ),
                ThreadComment(
                    id = 2L,
                    user = "Ryan Liu",
                    avatar = "RL",
                    time = "3 days ago",
                    text = "Would love to see a part 2 covering pagination for the replies. Is that planned?",
                    likes = 54,
                    replies = listOf(
                        ThreadReply(
                            id = 21L,
                            user = "Alex P.",
                            avatar = "AP",
                            time = "2 days ago",
                            text = "+1, pagination would be huge for performance when there are hundreds of replies.",
                            likes = 12
                        )
                    )
                ),
                ThreadComment(
                    id = 3L,
                    user = "maker_gal",
                    avatar = "MG",
                    time = "2 days ago",
                    text = "Great tutorial! The neon effect is so clean ✨",
                    likes = 324,
                    replies = emptyList()
                )
            )
        )
    }

    val totalCommentsCount = remember(comments) {
        comments.size + comments.sumOf { it.replies.size }
    }

    fun postComment() {
        val trimmed = textInput.trim()
        if (trimmed.isEmpty()) return

        val currentReplying = replyingTo
        if (currentReplying != null) {
            // Reply to thread
            comments = comments.map { c ->
                if (c.id == currentReplying.threadId) {
                    val newReply = ThreadReply(
                        id = System.currentTimeMillis(),
                        user = "You",
                        avatar = "Y",
                        time = "now",
                        text = trimmed,
                        likes = 0
                    )
                    c.copy(replies = c.replies + newReply)
                } else {
                    c
                }
            }
            // Auto expand thread if replying
            expandedThreads = expandedThreads + currentReplying.threadId
        } else {
            // Top level comment
            val newComment = ThreadComment(
                id = System.currentTimeMillis(),
                user = "You",
                avatar = "Y",
                time = "now",
                text = trimmed,
                likes = 0,
                replies = emptyList()
            )
            comments = listOf(newComment) + comments
        }
        textInput = ""
        replyingTo = null
    }

    // MAIN LAYOUT
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F14))
            .testTag("deila_play_screen_container")
    ) {
        if (loading) {
            // LOADING SKELETON MATCHING DeilaPlayScreen.js
            DeilaPlayLoadingSkeleton(
                commentsCountText = "Loading 12 comments...",
                onBackClick = onBackClick
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // ==========================================================
                // 1. VIDEO PLAYER (height: 210dp, background: #000)
                // ==========================================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .background(Color.Black)
                        .testTag("deila_video_player"),
                    contentAlignment = Alignment.Center
                ) {
                    // Playback visual background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0A0F1D),
                                        Color.Black,
                                        Color(0xFF071018)
                                    )
                                )
                            )
                    )

                    // Ambient Neon Glow when playing
                    if (isPlaying) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF00E5FF).copy(alpha = 0.25f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = size.minDimension * 0.7f
                                )
                            )
                        }
                    }

                    // Large Play / Pause button in center
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                            .testTag("video_player_play_toggle")
                    ) {
                        Text(
                            text = if (isPlaying) "⏸" else "▶",
                            color = Color(0xFF00E5FF),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Back button "← DEILA PLAY" in top-left
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 12.dp, top = 28.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xAA000000))
                            .clickable { onBackClick() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("btn_back_deila_play"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "← DEILA PLAY",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Top-right controls: re-trigger shimmer & mute
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 12.dp, top = 28.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { loading = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xAA000000))
                                .testTag("btn_reload_shimmer")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Test Shimmer",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { isMuted = !isMuted },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xAA000000))
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "Mute",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Bottom progress bar in player
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xCC000000))
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = String.format("%02d:%02d / %02d:%02d", currentSeconds / 60, currentSeconds % 60, totalSeconds / 60, totalSeconds % 60),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("4K NEON", color = Color(0xFF00E5FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        // Neon Progress Bar
                        LinearProgressIndicator(
                            progress = { currentSeconds.toFloat() / totalSeconds.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFF00E5FF),
                            trackColor = Color(0xFF333344)
                        )
                    }
                }

                // ==========================================================
                // 2. VIDEO INFO & ACTIONS
                // ==========================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = videoTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = channelInfo,
                        color = Color(0xFFAAAAAA),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action buttons (Like, Share, Save to Playlist)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Like Button
                        FilterChip(
                            selected = isLikedVideo,
                            onClick = {
                                isLikedVideo = !isLikedVideo
                                videoLikesCount += if (isLikedVideo) 1 else -1
                            },
                            label = {
                                Text(
                                    text = "$videoLikesCount",
                                    color = if (isLikedVideo) Color(0xFF00E5FF) else Color.White,
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isLikedVideo) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                    contentDescription = "Like",
                                    tint = if (isLikedVideo) Color(0xFF00E5FF) else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFF1A1A24),
                                selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.2f)
                            ),
                            border = null,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("btn_like_video")
                        )

                        // Save to Playlist Button
                        FilterChip(
                            selected = false,
                            onClick = { showPlaylistDialog = true },
                            label = { Text("Playlist", color = Color.White, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.PlaylistAdd,
                                    contentDescription = "Add to playlist",
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFF1A1A24)
                            ),
                            border = null,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("btn_save_playlist")
                        )

                        // Share
                        FilterChip(
                            selected = false,
                            onClick = { /* Share action */ },
                            label = { Text("Share", color = Color.White, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFF1A1A24)
                            ),
                            border = null,
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                // ==========================================================
                // 3. COMMENTS LIST (THREADED)
                // ==========================================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("threaded_comments_list")
                    ) {
                        // COMMENTS HEADER MATCHING DeilaPlayScreen.js
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F0F14))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                HorizontalDivider(color = Color(0xFF222222), thickness = 1.dp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Comments $totalCommentsCount",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Top comments ▾",
                                        color = Color(0xFF00E5FF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // Top-level comments
                        items(comments, key = { it.id }) { comment ->
                            ThreadCommentCard(
                                comment = comment,
                                isExpanded = expandedThreads.contains(comment.id),
                                onToggleExpand = {
                                    expandedThreads = if (expandedThreads.contains(comment.id)) {
                                        expandedThreads - comment.id
                                    } else {
                                        expandedThreads + comment.id
                                    }
                                },
                                onLike = {
                                    comments = comments.map { c ->
                                        if (c.id == comment.id) {
                                            val newLiked = !c.isLiked
                                            c.copy(
                                                isLiked = newLiked,
                                                likes = c.likes + if (newLiked) 1 else -1
                                            )
                                        } else c
                                    }
                                },
                                onReply = {
                                    replyingTo = ReplyingToState(
                                        threadId = comment.id,
                                        userName = comment.user
                                    )
                                },
                                onLikeReply = { replyId ->
                                    comments = comments.map { c ->
                                        if (c.id == comment.id) {
                                            c.copy(
                                                replies = c.replies.map { r ->
                                                    if (r.id == replyId) {
                                                        val newLiked = !r.isLiked
                                                        r.copy(
                                                            isLiked = newLiked,
                                                            likes = r.likes + if (newLiked) 1 else -1
                                                        )
                                                    } else r
                                                }
                                            )
                                        } else c
                                    }
                                }
                            )
                        }

                        // Bottom spacer for input bar
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }

                // ==========================================================
                // 4. BOTTOM INPUT BAR (STICKY)
                // ==========================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF14141E))
                        .border(1.dp, Color(0xFF222230), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("comment_input_bar")
                ) {
                    // Replying mode banner
                    AnimatedVisibility(
                        visible = replyingTo != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        val currentTarget = replyingTo
                        if (currentTarget != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF00E5FF).copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Replying to ",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "@${currentTarget.userName}",
                                        color = Color(0xFF00E5FF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                IconButton(
                                    onClick = { replyingTo = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel reply",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Input Field Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF).copy(alpha = 0.3f))
                                .border(1.dp, Color(0xFF00E5FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Y",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        // Text Field
                        TextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = {
                                Text(
                                    text = if (replyingTo != null) "Reply to @${replyingTo?.userName}..." else "Add a public comment...",
                                    color = Color(0xFF7A7A8C),
                                    fontSize = 13.sp
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF1E1E2C),
                                unfocusedContainerColor = Color(0xFF1E1E2C),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF00E5FF),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(20.dp),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("comment_text_field")
                        )

                        // Post / Send Button
                        IconButton(
                            onClick = { postComment() },
                            enabled = textInput.trim().isNotEmpty(),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (textInput.trim().isNotEmpty()) Color(0xFF00E5FF) else Color(0xFF2A2A38)
                                )
                                .testTag("btn_post_comment")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Post comment",
                                tint = if (textInput.trim().isNotEmpty()) Color.Black else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // ==============================================================
    // SAVE TO PLAYLIST DIALOG (Room DB Integration)
    // ==============================================================
    if (showPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showPlaylistDialog = false },
            containerColor = Color(0xFF1A1A24),
            title = {
                Text("Save Video to Playlist", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Select a Room DB playlist for offline playback:",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (playlists.isEmpty()) {
                        Text(
                            "No playlists created yet. Create your first playlist below!",
                            color = Color(0xFF00E5FF),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                        ) {
                            items(playlists) { playlist ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            onSaveToPlaylist?.invoke(playlist.id, videoId)
                                            showPlaylistDialog = false
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF252535)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.PlaylistPlay,
                                                contentDescription = null,
                                                tint = Color(0xFF00E5FF),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(playlist.name, color = Color.White, fontWeight = FontWeight.Medium)
                                        }
                                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.LightGray)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (showCreatePlaylistInput) {
                        OutlinedTextField(
                            value = newPlaylistName,
                            onValueChange = { newPlaylistName = it },
                            label = { Text("Playlist Name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = Color(0xFF00E5FF)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        TextButton(
                            onClick = { showCreatePlaylistInput = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF00E5FF))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Create New Playlist")
                        }
                    }
                }
            },
            confirmButton = {
                if (showCreatePlaylistInput) {
                    Button(
                        onClick = {
                            if (newPlaylistName.isNotBlank()) {
                                onCreatePlaylist?.invoke(newPlaylistName.trim())
                                newPlaylistName = ""
                                showCreatePlaylistInput = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black)
                    ) {
                        Text("Create")
                    }
                } else {
                    Button(
                        onClick = { showPlaylistDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A3A))
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            },
            dismissButton = {
                if (showCreatePlaylistInput) {
                    TextButton(onClick = { showCreatePlaylistInput = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            }
        )
    }
}

/**
 * ThreadCommentCard - Single top-level comment card with replies indentation
 */
@Composable
fun ThreadCommentCard(
    comment: ThreadComment,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onLike: () -> Unit,
    onReply: () -> Unit,
    onLikeReply: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("comment_${comment.id}")
    ) {
        // Main Comment Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2A3A))
                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = comment.avatar,
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Body
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = comment.user,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = comment.time,
                        color = Color(0xFF888899),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = comment.text,
                    color = Color(0xFFDDDDDD),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Actions: Like, Reply
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onLike() }
                            .padding(2.dp)
                    ) {
                        Icon(
                            imageVector = if (comment.isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "Like",
                            tint = if (comment.isLiked) Color(0xFF00E5FF) else Color(0xFFAAAAAA),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${comment.likes}",
                            color = if (comment.isLiked) Color(0xFF00E5FF) else Color(0xFFAAAAAA),
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = "Reply",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clickable { onReply() }
                            .padding(2.dp)
                    )
                }

                // Replies Toggle & List
                if (comment.replies.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isExpanded) "Hide replies ▴" else "View ${comment.replies.size} replies ▾",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clickable { onToggleExpand() }
                            .padding(vertical = 4.dp)
                    )

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            comment.replies.forEach { reply ->
                                ThreadReplyItem(
                                    reply = reply,
                                    onLike = { onLikeReply(reply.id) },
                                    onReplyToReply = { onReply() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * ThreadReplyItem - Nested reply with author badge and indentation
 */
@Composable
fun ThreadReplyItem(
    reply: ThreadReply,
    onLike: () -> Unit,
    onReplyToReply: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Reply Avatar
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (reply.isAuthor) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF222230))
                .border(
                    1.dp,
                    if (reply.isAuthor) Color(0xFF00E5FF) else Color(0xFF444455),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = reply.avatar,
                color = if (reply.isAuthor) Color(0xFF00E5FF) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = reply.user,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                if (reply.isAuthor) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF00E5FF))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "Author",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }
                Text(
                    text = reply.time,
                    color = Color(0xFF888899),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = reply.text,
                color = Color(0xFFCCCCCC),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onLike() }
                        .padding(2.dp)
                ) {
                    Icon(
                        imageVector = if (reply.isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Like",
                        tint = if (reply.isLiked) Color(0xFF00E5FF) else Color(0xFFAAAAAA),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${reply.likes}",
                        color = if (reply.isLiked) Color(0xFF00E5FF) else Color(0xFFAAAAAA),
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "Reply",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { onReplyToReply() }
                        .padding(2.dp)
                )
            }
        }
    }
}

/**
 * DeilaPlayLoadingSkeleton - Loading skeleton matching DeilaPlayScreen.js
 */
@Composable
fun DeilaPlayLoadingSkeleton(
    commentsCountText: String = "Loading 12 comments...",
    onBackClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF2A2A3A),
            Color(0xFF00E5FF).copy(alpha = 0.35f),
            Color(0xFF2A2A3A)
        ),
        start = Offset(shimmerProgress * 500f, 0f),
        end = Offset((shimmerProgress + 0.5f) * 500f, 0f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F14))
            .padding(16.dp)
            .padding(top = 28.dp)
            .testTag("deila_play_loading_skeleton")
    ) {
        // Back header in skeleton
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF00E5FF)
                )
            }
            Text("DEILA PLAY", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        // Header card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1A1A24))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Comments",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
            Text(
                text = "Loading...",
                color = Color(0xFF00E5FF),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4 Skeleton items
        repeat(4) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A1A24))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                // 50x50 round avatar skeleton
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(shimmerBrush)
                )

                // 3 skeleton text bars
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(18.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(shimmerBrush)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(shimmerBrush)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(shimmerBrush)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = commentsCountText,
            color = Color(0xFF00E5FF).copy(alpha = 0.55f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
