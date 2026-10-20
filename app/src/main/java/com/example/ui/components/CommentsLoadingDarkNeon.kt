package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * CommentsLoadingDarkNeon - Style 1 Dark Neon Shimmer skeleton loading state
 * Corresponds to CommentsLoading.js
 */
@Composable
fun CommentsLoadingDarkNeon(
    modifier: Modifier = Modifier,
    commentsCountText: String = "Loading 12 comments..."
) {
    // 1200ms looping neon shimmer animation
    val infiniteTransition = rememberInfiniteTransition(label = "neon_shimmer")
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_sweep"
    )

    // Shimmer Brush for skeleton slots
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF2A2A3A),
            Color(0xFF00E5FF).copy(alpha = 0.40f),
            Color(0xFF2A2A3A)
        ),
        start = Offset(x = shimmerProgress * 600f, y = 0f),
        end = Offset(x = (shimmerProgress + 0.5f) * 600f, y = 0f)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F14))
            .padding(16.dp)
            .testTag("comments_loading_dark_neon")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1A1A24))
                .padding(12.dp)
                .testTag("comments_loading_header"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Comments",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E5FF))
                )
                Text(
                    text = "Loading...",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Skeleton Items List
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(4) { idx ->
                SkeletonItem(brush = shimmerBrush, tag = "skeleton_comment_$idx")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input skeleton placeholder
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(25.dp))
                .background(Color(0xFF1A1A24))
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.15f), RoundedCornerShape(25.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("input_skeleton_placeholder"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add a comment...",
                color = Color(0xFF666666),
                fontSize = 13.sp
            )
            Text(
                text = "➤",
                color = Color(0xFF00E5FF),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = commentsCountText,
            color = Color(0xFF00E5FF).copy(alpha = 0.55f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
        )
    }
}

@Composable
private fun SkeletonItem(
    brush: Brush,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A24))
            .padding(14.dp)
            .testTag(tag),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar circle shimmer
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(brush)
        )

        // Text lines shimmer
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Line 1 (Name / title)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.60f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(brush)
            )

            // Line 2 (Comment body)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(brush)
            )

            // Line 3 (Comment subtext / time)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.40f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(brush)
            )
        }
    }
}
