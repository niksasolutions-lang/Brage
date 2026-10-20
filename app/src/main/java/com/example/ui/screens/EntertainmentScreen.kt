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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.DownloadedVideo
import com.example.data.local.Playlist
import com.example.ui.components.DeilaPlayIcon

data class PlayMediaItem(
    val id: String,
    val title: String,
    val category: String,
    val duration: String,
    val description: String = "",
    val views: String,
    val isDownloaded: Boolean = false,
    val badge: String = "4K HDR",
    val channelName: String = "PeakTrail",
    val channelAvatarColor: Color = Color(0xFF00E5FF),
    val uploadTime: String = "2 days ago",
    val thumbUrl: String = "https://picsum.photos/400/225?1"
)

data class DeilaShortItem(
    val id: String,
    val title: String,
    val duration: String,
    val thumbUrl: String,
    val views: String = "120K"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntertainmentScreen(
    isLoading: Boolean,
    isDownloading: Boolean,
    isRefreshing: Boolean,
    onTriggerRefresh: () -> Unit,
    onTriggerDownload: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onOpenDeilaAiChat: () -> Unit,
    onBackClick: () -> Unit = {},
    onOpenVideoPlay: (PlayMediaItem) -> Unit = {},
    onOpenDeilaPlayFinal: (PlayMediaItem) -> Unit = {},
    onOpenShortPage: (String) -> Unit = {},
    onOpenDownloadedVideo: (DownloadedVideo) -> Unit = {},
    downloadedVideos: List<DownloadedVideo> = emptyList(),
    downloadProgress: Map<String, Float> = emptyMap(),
    playlists: List<Playlist> = emptyList(),
    onCreatePlaylist: (String) -> Unit = {},
    onDeletePlaylist: (Long) -> Unit = {},
    onAddVideoToPlaylist: (Long, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    // Intercept hardware/gesture back to leave DEILA PLAY and show normal Brage bottom bar
    BackHandler {
        onBackClick()
    }

    var activeChip by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedVideoForMenu by remember { mutableStateOf<PlayMediaItem?>(null) }
    var showCastNotification by remember { mutableStateOf(false) }

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistInputName by remember { mutableStateOf("") }
    var selectedPlaylistForView by remember { mutableStateOf<Playlist?>(null) }
    var videoForPlaylistAddition by remember { mutableStateOf<PlayMediaItem?>(null) }

    val chips = listOf("All", "Music", "Live", "Nandi Hills", "Brage Tips", "Home DIY", "Tech")

    val isBusy = isLoading || isDownloading || isRefreshing

    val videos = remember {
        listOf(
            PlayMediaItem(
                id = "synth_neon",
                title = "How I Built This Neon Synth in 24 Hours | DIY Electronics",
                category = "Tech",
                duration = "18:40",
                description = "Designing a custom analog synthesizer with glowing neon electroluminescent wire, custom PCB etching, and interactive threading logic.",
                views = "87K",
                channelName = "DIY Electronics",
                channelAvatarColor = Color(0xFF00E5FF),
                uploadTime = "2 days ago",
                thumbUrl = "https://picsum.photos/400/225?7",
                badge = "FEATURED"
            ),
            PlayMediaItem(
                id = "media_nandi",
                title = "Sunrise Trek at Nandi Hills | Complete Guide",
                category = "Nandi Hills",
                duration = "12:34",
                description = "Complete morning trek checklist, vantage viewpoints, misty sunrise drone footage, and parking tips for Nandi Hills.",
                views = "128K",
                channelName = "PeakTrail",
                channelAvatarColor = Color(0xFF00E5FF),
                uploadTime = "2 days ago",
                thumbUrl = "https://picsum.photos/400/225?1",
                badge = "TRENDING"
            ),
            PlayMediaItem(
                id = "media_lofi",
                title = "Lo-fi Beats for Focus & Study | DEILA PLAY Mix",
                category = "Music",
                duration = "8:21",
                description = "Mellow analog synthesizer waves and rain ambience mixed by Deila Acoustic Labs for deep focus and study.",
                views = "56K",
                channelName = "Chillwave Beats",
                channelAvatarColor = Color(0xFF2CF6FF),
                uploadTime = "5 hours ago",
                thumbUrl = "https://picsum.photos/400/225?2",
                badge = "DEILA MIX"
            ),
            PlayMediaItem(
                id = "media_1",
                title = "Cyber Electric Diagnosis & Motor Windings 101",
                category = "Tech",
                duration = "14:20",
                description = "Master Electrician breakdown of modern brushless motors and circuit diagnostics with Deila AI telemetry.",
                views = "24.8K",
                channelName = "Marcus Chen (Master Tech)",
                channelAvatarColor = Color(0xFF38BDF8),
                uploadTime = "2 hours ago",
                thumbUrl = "https://picsum.photos/400/225?3",
                badge = "DEILA ORIGINAL"
            ),
            PlayMediaItem(
                id = "media_2",
                title = "Sub-Zero HVAC Repair: Step-by-Step Compressor Purge",
                category = "Home DIY",
                duration = "22:15",
                description = "Watch Master Tech Marcus Chen overhaul a high-efficiency inverter heat pump in real-time.",
                views = "18.3K",
                channelName = "SubZero Pro Services",
                channelAvatarColor = Color(0xFF67E8F9),
                uploadTime = "1 day ago",
                thumbUrl = "https://picsum.photos/400/225?4",
                badge = "POPULAR"
            ),
            PlayMediaItem(
                id = "media_live_dispatch",
                title = "Live 24/7 Service Dispatch Telemetry & Emergency Pro Stream",
                category = "Live",
                duration = "LIVE",
                description = "Real-time dispatch coordination and service readiness across the metro area.",
                views = "9.4K",
                channelName = "Brage Ops Live",
                channelAvatarColor = Color(0xFFEF4444),
                uploadTime = "Streaming now",
                thumbUrl = "https://picsum.photos/400/225?5",
                badge = "LIVE NOW"
            ),
            PlayMediaItem(
                id = "media_brage_tips",
                title = "Top 5 Home Maintenance Hacks Every Homeowner Must Know",
                category = "Brage Tips",
                duration = "10:05",
                description = "Prevent costly water leaks, AC refrigerant drop, and circuit breaker tripping with pro maintenance routines.",
                views = "47K",
                channelName = "Brage Pro Academy",
                channelAvatarColor = Color(0xFF00E5FF),
                uploadTime = "3 days ago",
                thumbUrl = "https://picsum.photos/400/225?6",
                badge = "ESSENTIAL"
            )
        )
    }

    val shorts = remember {
        listOf(
            DeilaShortItem("s1", "Nandi Hills Sunrise in 60s", "60s", "https://picsum.photos/200/300?1", "124K"),
            DeilaShortItem("s2", "Lo-Fi Vinyl Scratch Ambience", "60s", "https://picsum.photos/200/300?2", "98K"),
            DeilaShortItem("s3", "Multimeter Beep Test in 15s", "45s", "https://picsum.photos/200/300?3", "210K"),
            DeilaShortItem("s4", "Quick PEX Crimp Trick", "30s", "https://picsum.photos/200/300?4", "145K")
        )
    }

    // Dynamic Filter
    val filteredVideos = remember(activeChip, searchQuery, videos) {
        videos.filter { v ->
            val matchesChip = if (activeChip == "All") true else v.category.equals(activeChip, ignoreCase = true)
            val matchesSearch = if (searchQuery.isBlank()) true else {
                v.title.contains(searchQuery, ignoreCase = true) ||
                        v.channelName.contains(searchQuery, ignoreCase = true) ||
                        v.category.contains(searchQuery, ignoreCase = true)
            }
            matchesChip && matchesSearch
        }
    }

    // YouTube-style background: #0f0f0f
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .testTag("deila_play_youtube_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ==============================================================
            // 1. HEADER like YouTube with Back Button & ▶ DEILA PLAY
            // ==============================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .clickable(
                            onClick = { onBackClick() },
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        )
                        .padding(end = 12.dp)
                        .testTag("btn_header_back_deila")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    DeilaPlayIcon(
                        isBusy = isBusy,
                        isSelected = true,
                        size = 22.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Black)) {
                                append("DEILA ")
                            }
                            withStyle(SpanStyle(color = Color(0xFF00E5FF), fontWeight = FontWeight.Black)) {
                                append("PLAY")
                            }
                        },
                        fontSize = 22.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showCastNotification = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cast,
                            contentDescription = "Cast",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenDeilaAiChat,
                        modifier = Modifier.size(36.dp).testTag("btn_header_deila_ai")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Deila AI",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { /* notification */ },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ==============================================================
            // 2. SEARCH BAR: YouTube Style
            // ==============================================================
            Surface(
                shape = RoundedCornerShape(25.dp),
                color = Color(0xFF212121),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3300E5FF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .height(44.dp)
                    .testTag("deila_search_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔍",
                        fontSize = 14.sp,
                        color = Color(0xFF888888)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search on DEILA PLAY",
                                color = Color(0xFF888888),
                                fontSize = 13.sp
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                            cursorBrush = SolidColor(Color(0xFF00E5FF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_search_text")
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color(0xFF888888),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // ==============================================================
            // 3. CHIPS: YouTube Style Horizontal Scroll
            // ==============================================================
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .testTag("deila_chips_row"),
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chips) { chip ->
                    val isSelected = activeChip == chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF212121),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4400E5FF)),
                        modifier = Modifier
                            .clickable { activeChip = chip }
                            .testTag("chip_$chip")
                    ) {
                        Text(
                            text = if (chip == "Live") "● Live" else chip,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Pipeline Sync Indicator (Pulsing download / refresh)
            if (isBusy) {
                Surface(
                    color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF00E5FF)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isDownloading) "Downloading 4K Offline Stream..." else "Syncing Deila Play Feed...",
                                fontSize = 11.sp,
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "SYNCING",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00E5FF)
                        )
                    }
                }
            }

            // ==============================================================
            // 4. MAIN SCROLL: SHORTS & YOUTUBE VIDEO FEED
            // ==============================================================
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("youtube_video_scroll_feed"),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Interactive Refresh / Download Quick Action Bar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onTriggerRefresh,
                            enabled = !isRefreshing,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("btn_refresh_youtube_feed"),
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isRefreshing) Color(0xFF00E5FF) else Color(0xFF333333)
                            )
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isRefreshing) "Refreshing..." else "Refresh Feed", fontSize = 11.sp, color = Color.White)
                        }

                        Button(
                            onClick = { onTriggerDownload("pack_id", "YouTube Pack", "YouTube", "https://picsum.photos/400/800?1") },
                            enabled = !isDownloading,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("btn_download_youtube_pack"),
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDownloading) Color(0xFF00E5FF) else Color(0xFF212121)
                            )
                        ) {
                            Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isDownloading) "Downloading..." else "Offline Pack", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }

                // ==============================================================
                // MY DOWNLOADS SECTION
                // ==============================================================
                if (downloadedVideos.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "My Downloads",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(downloadedVideos) { v ->
                                    Card(
                                        modifier = Modifier
                                            .width(160.dp)
                                            .height(100.dp)
                                            .clickable { onOpenDownloadedVideo(v) }
                                            .testTag("downloaded_video_${v.id}"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF212121))
                                    ) {
                                        Column {
                                            Text(v.title, color = Color.White, fontSize = 12.sp, maxLines = 1, modifier = Modifier.padding(8.dp))
                                            Text(v.channel, color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==============================================================
                // PLAYLISTS SECTION (Room Database)
                // ==============================================================
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlaylistPlay,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Playlists",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                            }

                            Text(
                                text = "+ New Playlist",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .clickable { showCreatePlaylistDialog = true }
                                    .padding(4.dp)
                                    .testTag("btn_create_playlist")
                            )
                        }

                        if (playlists.isEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showCreatePlaylistDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF00E5FF))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Organize videos into Playlists", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Tap to create your first Room database custom playlist", color = Color.Gray, fontSize = 11.sp)
                                    }
                                }
                            }
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(playlists) { p ->
                                    Card(
                                        modifier = Modifier
                                            .width(170.dp)
                                            .clickable { selectedPlaylistForView = p }
                                            .testTag("playlist_card_${p.id}"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2C))
                                    ) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(80.dp)
                                                    .background(
                                                        Brush.linearGradient(
                                                            listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlaylistPlay,
                                                    contentDescription = null,
                                                    tint = Color(0xFF00E5FF),
                                                    modifier = Modifier.size(36.dp)
                                                )
                                            }
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = p.name,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Room DB Playlist",
                                                    color = Color(0xFF00E5FF),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==============================================================
                // SHORTS SHELF
                // ==============================================================
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Shorts  ▶",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                            }
                            Text(
                                text = "See all ›",
                                color = Color(0xFF00E5FF),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { onOpenShortPage(shorts.firstOrNull()?.id ?: "s1") }
                                    .testTag("btn_shorts_see_all")
                            )
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(shorts) { s ->
                                Box(
                                    modifier = Modifier
                                        .width(110.dp)
                                        .height(170.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF212121))
                                        .clickable {
                                            onOpenShortPage(s.id)
                                        }
                                        .testTag("short_item_${s.id}")
                                ) {
                                    // Async Image with fallback texture
                                    AsyncImage(
                                        model = s.thumbUrl,
                                        contentDescription = s.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Gradient overlay for readability
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawRect(
                                            brush = Brush.verticalGradient(
                                                listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.75f)
                                                )
                                            )
                                        )
                                    }

                                    // Duration badge at bottom-right
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color.Black.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            text = s.duration,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(3.dp)
                                        )
                                    }

                                    // Title at bottom-left
                                    Text(
                                        text = s.title,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(6.dp)
                                            .padding(end = 30.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ==============================================================
                // VIDEO FEED - YouTube Style
                // ==============================================================
                items(filteredVideos) { v ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenVideoPlay(v) }
                            .padding(bottom = 16.dp)
                            .testTag("video_feed_item_${v.id}")
                    ) {
                        // Thumbnail with 210.dp height
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .background(Color(0xFF212121))
                        ) {
                            AsyncImage(
                                model = v.thumbUrl,
                                contentDescription = v.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Subtle fallback canvas in case image is loading
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.6f)
                                        )
                                    )
                                )
                            }

                            // Center Play Icon Indicator
                            Box(
                                modifier = Modifier.align(Alignment.Center),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }

                            // Duration badge bottom right
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Black.copy(alpha = 0.85f),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = v.duration,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            // Top left badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF0A4A8A).copy(alpha = 0.85f),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = v.badge,
                                    color = Color(0xFF00E5FF),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Video Info Row (Avatar, Title, Channel info, 3-dots)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Circular Avatar with #00e5ff
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(v.channelAvatarColor)
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = v.channelName.take(2).uppercase(),
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = v.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${v.channelName} • ${v.views} views • ${v.uploadTime}",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 12.sp
                                )
                            }

                            // ⋮ 3-dots more menu
                            Box {
                                // Progress overlay if downloading
                                if (downloadProgress.containsKey(v.id)) {
                                    val progress = downloadProgress[v.id] ?: 0f
                                    CircularProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .padding(4.dp),
                                        color = Color(0xFF00E5FF),
                                        strokeWidth = 2.dp,
                                        trackColor = Color.White.copy(alpha = 0.2f)
                                    )
                                } else {
                                    IconButton(
                                        onClick = { onTriggerDownload(v.id, v.title, v.channelName, v.thumbUrl) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("btn_download_${v.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Download,
                                            contentDescription = "Download",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Cast toast notification
        if (showCastNotification) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = Color(0xFF1E293B),
                contentColor = Color(0xFF00E5FF),
                action = {
                    TextButton(onClick = { showCastNotification = false }) {
                        Text("OK", color = Color(0xFF00E5FF))
                    }
                }
            ) {
                Text("Searching for wireless displays & TV targets...")
            }
        }

        // More options Modal Bottom Sheet
        selectedVideoForMenu?.let { video ->
            ModalBottomSheet(
                onDismissRequest = { selectedVideoForMenu = null },
                containerColor = Color(0xFF212121)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = video.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val menuActions = listOf(
                        Icons.Outlined.PlayArrow to "Play in DeilaPlay (Threaded Comments)",
                        Icons.Default.PlaylistAdd to "Add to Playlist",
                        Icons.Outlined.Download to "Download Offline Pack",
                        Icons.Outlined.BookmarkBorder to "Save to Watch Later",
                        Icons.Outlined.Share to "Share Video Stream",
                        Icons.Default.AutoAwesome to "Ask Deila AI about this video"
                    )

                    menuActions.forEach { (icon, text) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val currentVideo = video
                                    selectedVideoForMenu = null
                                    if (text.contains("DeilaPlay")) {
                                        onOpenDeilaPlayFinal(currentVideo)
                                    } else if (text.contains("Add to Playlist")) {
                                        videoForPlaylistAddition = currentVideo
                                    } else if (text.contains("Download")) {
                                        onTriggerDownload(currentVideo.id, currentVideo.title, currentVideo.channelName, currentVideo.thumbUrl)
                                    } else if (text.contains("Ask Deila AI")) {
                                        onOpenDeilaAiChat()
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = text, color = Color.White, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Create Playlist Dialog
        if (showCreatePlaylistDialog) {
            AlertDialog(
                onDismissRequest = { showCreatePlaylistDialog = false },
                containerColor = Color(0xFF1E1E2C),
                title = { Text("Create New Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Create a custom list saved to the Room database:", color = Color.LightGray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = newPlaylistInputName,
                            onValueChange = { newPlaylistInputName = it },
                            label = { Text("Playlist Name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = Color(0xFF00E5FF)
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_playlist_name")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPlaylistInputName.isNotBlank()) {
                                onCreatePlaylist(newPlaylistInputName.trim())
                                newPlaylistInputName = ""
                                showCreatePlaylistDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                        modifier = Modifier.testTag("btn_confirm_create_playlist")
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreatePlaylistDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }

        // Add Video to Playlist Dialog
        videoForPlaylistAddition?.let { targetVideo ->
            AlertDialog(
                onDismissRequest = { videoForPlaylistAddition = null },
                containerColor = Color(0xFF1E1E2C),
                title = { Text("Add to Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Add \"${targetVideo.title}\" to:",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        if (playlists.isEmpty()) {
                            Text("No playlists yet. Tap below to create one.", color = Color(0xFF00E5FF), fontSize = 12.sp)
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                                items(playlists) { p ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                onAddVideoToPlaylist(p.id, targetVideo.id)
                                                videoForPlaylistAddition = null
                                            },
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF28283C))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(p.name, color = Color.White, fontWeight = FontWeight.Medium)
                                            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF00E5FF))
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        videoForPlaylistAddition = null
                        showCreatePlaylistDialog = true
                    }) {
                        Text("+ New Playlist", color = Color(0xFF00E5FF))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { videoForPlaylistAddition = null }) {
                        Text("Close", color = Color.Gray)
                    }
                }
            )
        }

        // View Playlist Dialog
        selectedPlaylistForView?.let { playlist ->
            AlertDialog(
                onDismissRequest = { selectedPlaylistForView = null },
                containerColor = Color(0xFF1E1E2C),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(playlist.name, color = Color.White, fontWeight = FontWeight.Bold)
                        IconButton(onClick = {
                            onDeletePlaylist(playlist.id)
                            selectedPlaylistForView = null
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                },
                text = {
                    Column {
                        Text("Room Database Playlist", color = Color(0xFF00E5FF), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Organize offline videos into this list for instant playback in DEILA PLAY.", color = Color.LightGray, fontSize = 13.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { selectedPlaylistForView = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black)
                    ) {
                        Text("Done")
                    }
                }
            )
        }
    }
}
