package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * DEILA PLAY ICON - GLOWS WHEN BUSY
 *
 * Implements the pulsating neon cyan glow (#00e5ff) when busy (isLoading, isDownloading, isRefreshing),
 * smooth scale pulsing animation, solid play circle icon when busy / selected, and outline when idle.
 */
@Composable
fun DeilaPlayIcon(
    isBusy: Boolean,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    // Pulse animation when busy
    val infiniteTransition = rememberInfiniteTransition(label = "deila_play_pulse")

    val pulseScale by if (isBusy) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
    } else {
        rememberUpdatedState(newValue = if (isSelected) 1.05f else 1.0f)
    }

    val glowAlpha by if (isBusy) {
        infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowAlpha"
        )
    } else {
        rememberUpdatedState(newValue = 0f)
    }

    val iconColor = when {
        isBusy -> Color(0xFF00E5FF)
        isSelected -> Color(0xFF0A4A8A) // Match request: #0A4A8A when focused/selected, or cyan
        else -> Color(0xFF5A5A5A)
    }

    Box(
        modifier = modifier
            .size(32.dp)
            .testTag("deila_play_icon_container"),
        contentAlignment = Alignment.Center
    ) {
        // Glowing halo effect when busy
        if (isBusy) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(Color(0xFF00E5FF).copy(alpha = 0.15f * glowAlpha))
                    .border(
                        width = 1.dp,
                        color = Color(0xFF00E5FF).copy(alpha = 0.4f * glowAlpha),
                        shape = CircleShape
                    )
            )
        }

        // Inner glowing shadow simulation using ambient shadow + icon
        Box(
            modifier = Modifier
                .size(size)
                .scale(pulseScale)
                .then(
                    if (isBusy) {
                        Modifier.shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            spotColor = Color(0xFF00E5FF),
                            ambientColor = Color(0xFF00E5FF)
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isBusy || isSelected) {
                    Icons.Filled.PlayCircle
                } else {
                    Icons.Outlined.PlayCircleOutline
                },
                contentDescription = "DEILA PLAY",
                tint = iconColor,
                modifier = Modifier
                    .size(size)
                    .testTag("deila_play_icon_vector")
            )
        }
    }
}
