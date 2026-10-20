package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class LightCommentItem(
    val id: Long,
    val name: String,
    val avatar: String,
    val colorHex: String,
    val time: String,
    val text: String,
    val likes: Int,
    val isLiked: Boolean = false,
    val verified: Boolean = false,
    val replyTo: Long? = null
)

private fun parseHexColor(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = cleanHex.toLong(16)
        if (cleanHex.length == 6) {
            Color(0xFF000000 or colorInt)
        } else {
            Color(colorInt)
        }
    } catch (e: Exception) {
        Color(0xFFA8CFF0)
    }
}

/**
 * CommentsLightStyle - Clean, bright, modern comments UI corresponding to CommentsLightStyle.js
 * Supports replies, verified creators, like counters, and interactive comment publishing.
 */
@Composable
fun CommentsLightStyleContent(
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null,
    postTitle: String = "DEILA PLAY • 2h",
    postSubtitle: String = "Our new short film is live now ✨ #DEILAPLAY #NowPlaying"
) {
    var comments by remember {
        mutableStateOf(
            listOf(
                LightCommentItem(
                    id = 1L,
                    name = "mika.lee",
                    avatar = "MK",
                    colorHex = "#A8CFF0",
                    time = "1h",
                    text = "This is so beautifully filmed! The sound design is incredible 🔥",
                    likes = 12,
                    replyTo = null
                ),
                LightCommentItem(
                    id = 2L,
                    name = "deila_official",
                    avatar = "D",
                    colorHex = "#FFC88A",
                    time = "1h",
                    text = "Thank you so much Mika! Means a lot to the team 💛",
                    likes = 34,
                    verified = true,
                    replyTo = 1L
                ),
                LightCommentItem(
                    id = 3L,
                    name = "jordanp",
                    avatar = "J",
                    colorHex = "#A8D8B9",
                    time = "45m",
                    text = "Been waiting for this release all week. Love it!",
                    likes = 5,
                    replyTo = null
                ),
                LightCommentItem(
                    id = 4L,
                    name = "ariel_k",
                    avatar = "A",
                    colorHex = "#D0A8E8",
                    time = "22m",
                    text = "The visuals are stunning, can't wait for more from DEILA PLAY!",
                    likes = 2,
                    replyTo = null
                )
            )
        )
    }

    var text by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<LightCommentItem?>(null) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .testTag("comments_light_style_container"),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Drag handle pill for sheet styling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFE0E0E0))
                )
            }

            // 1. Header (Comments title & options / close)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comments",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF333333)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        IconButton(
                            onClick = { showOptionsMenu = !showOptionsMenu },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_comments_more_options")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Options",
                                tint = Color(0xFF999999)
                            )
                        }
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Simulate Dark Neon Shimmer") },
                                onClick = {
                                    showOptionsMenu = false
                                    isLoading = true
                                    coroutineScope.launch {
                                        delay(1500)
                                        isLoading = false
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Top comments") },
                                onClick = {
                                    comments = comments.sortedByDescending { it.likes }
                                    showOptionsMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Newest first") },
                                onClick = {
                                    comments = comments.sortedByDescending { it.id }
                                    showOptionsMenu = false
                                }
                            )
                        }
                    }

                    if (onClose != null) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_comments_close")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF666666)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

            // 2. Post / Media context banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFCFCFC))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pink rounded media badge with ▶✨
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFFFE6F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶✨",
                        fontSize = 20.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = postTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF222222),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = postSubtitle,
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 3. Count Section
            Text(
                text = "Comments • ${comments.size}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    CommentsLoadingDarkNeon(
                        modifier = Modifier.fillMaxSize(),
                        commentsCountText = "Loading ${comments.size} comments..."
                    )
                }
            } else {
                // 4. Scrollable Comments List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("comments_light_list"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    items(comments, key = { it.id }) { c ->
                        val isReplying = c.replyTo != null
                        val avatarBg = parseHexColor(c.colorHex)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .padding(start = if (isReplying) 32.dp else 0.dp)
                                .testTag("comment_item_${c.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Avatar Badge
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(avatarBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = c.avatar,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                }

                                // Author, time, verified, content & actions
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = c.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF222222)
                                        )

                                        if (c.verified) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFF00E5FF),
                                                modifier = Modifier.size(14.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Verified Creator",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "• ${c.time}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF999999)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Text(
                                        text = c.text,
                                        fontSize = 13.sp,
                                        color = Color(0xFF333333),
                                        lineHeight = 18.sp
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Reply & Like Action row
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // Reply action button
                                        Text(
                                            text = "Reply",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF0A4A8A),
                                            modifier = Modifier
                                                .clickable { replyingTo = c }
                                                .testTag("btn_reply_comment_${c.id}")
                                        )

                                        // Like counter toggle
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clickable {
                                                    comments = comments.map { item ->
                                                        if (item.id == c.id) {
                                                            val nextLiked = !item.isLiked
                                                            val nextLikes = if (nextLiked) item.likes + 1 else (item.likes - 1).coerceAtLeast(0)
                                                            item.copy(isLiked = nextLiked, likes = nextLikes)
                                                        } else item
                                                    }
                                                }
                                                .testTag("btn_like_comment_${c.id}")
                                        ) {
                                            Icon(
                                                imageVector = if (c.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                                contentDescription = "Like",
                                                tint = if (c.isLiked) Color(0xFFFF2D55) else Color(0xFF888888),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${c.likes}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF888888)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Replying Banner (when active)
            AnimatedVisibility(
                visible = replyingTo != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                replyingTo?.let { target ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF3F4F6))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Replying to @${target.name}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0A4A8A)
                        )
                        IconButton(
                            onClick = { replyingTo = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Reply",
                                tint = Color(0xFF888888),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 6. Comment Input Bar at the Bottom
            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // User Avatar
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                }

                // Text Input Box
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = {
                        Text(
                            text = if (replyingTo != null) "Reply to @${replyingTo?.name}..." else "Add a comment...",
                            fontSize = 13.sp,
                            color = Color(0xFF999999)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(22.dp))
                        .testTag("input_comment_light_text"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF00E5FF)
                    ),
                    shape = RoundedCornerShape(22.dp),
                    singleLine = true
                )

                // Post Button (▷ / Send)
                val canPost = text.isNotBlank()
                Surface(
                    shape = CircleShape,
                    color = if (canPost) Color(0xFF00E5FF) else Color(0xFFE5E7EB),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(enabled = canPost) {
                            val newComment = LightCommentItem(
                                id = System.currentTimeMillis(),
                                name = "You",
                                avatar = "YO",
                                colorHex = "#93C5FD",
                                time = "just now",
                                text = text.trim(),
                                likes = 0,
                                isLiked = false,
                                replyTo = replyingTo?.id
                            )
                            comments = listOf(newComment) + comments
                            text = ""
                            replyingTo = null
                        }
                        .testTag("btn_comment_light_send")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (canPost) Color(0xFF030811) else Color(0xFF999999),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modal Bottom Sheet wrapping CommentsLightStyle
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsLightBottomSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    postTitle: String = "DEILA PLAY • 2h",
    postSubtitle: String = "Our new short film is live now ✨ #DEILAPLAY #NowPlaying"
) {
    if (isOpen) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            CommentsLightStyleContent(
                onClose = onDismiss,
                postTitle = postTitle,
                postSubtitle = postSubtitle,
                modifier = Modifier.fillMaxHeight(0.78f)
            )
        }
    }
}
