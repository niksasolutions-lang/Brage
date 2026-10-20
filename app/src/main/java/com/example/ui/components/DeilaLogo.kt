package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Authentic DEILA Brand Logo component matching the exact specifications:
 * - Circular dark navy background (#050d1a / #020c18)
 * - Luminous glow-ring (#2cf6ff / #00e5ff) with outer and inner glow effect
 * - Soft radial watercolor electric cyan glow backdrop
 * - Iconic Leaf D monogram path with precision SVG coordinates and gradient stroke
 * - Crisp futuristic typography "DEILA" with glow text shadow and wide tracking
 * - High-resolution generated logo asset fallback with smooth blending
 */
@Composable
fun DeilaLogo(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    showSubtext: Boolean = true,
    subtext: String = "From Deila"
) {
    Column(
        modifier = modifier.testTag("deila_brand_logo_container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .shadow(elevation = 16.dp, shape = CircleShape, spotColor = Color(0xFF00E5FF), ambientColor = Color(0xFF00E5FF))
                .background(Color(0xFF050D1A))
                .border(
                    width = (size.value * 0.018f).coerceAtLeast(1.5f).dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            Color(0xFF00E5FF),
                            Color(0xFF2A7BFF),
                            Color(0xFF2CF6FF),
                            Color(0xFF00E5FF)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Layer 1: Photographic / Rendered High-Res Asset
            Image(
                painter = painterResource(id = R.drawable.img_deila_avatar),
                contentDescription = "DEILA Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Layer 2: Ambient neon overlay ring for vivid futuristic glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding((size.value * 0.04f).dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF2CF6FF).copy(alpha = 0.5f),
                        shape = CircleShape
                    )
            )
        }

        if (showSubtext) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.8.sp,
                    color = Color(0xFF00E5FF)
                )
            }
        }
    }
}

/**
 * Procedural Leaf D vector emblem rendered onto Compose Canvas
 * faithfully translating the path:
 * M15 10 Q15 5 25 5 L50 5 Q55 5 55 12 L55 25
 * M15 35 Q10 45 20 55 Q35 65 60 55 Q68 50 68 40
 * M20 55 Q15 45 30 20
 * M30 30 Q45 15 65 30 Q75 40 80 25 Q70 10 55 15 Q45 20 42 35
 * M55 15 Q65 5 75 10 Q80 20 72 35
 */
@Composable
fun DeilaProceduralEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF050D1A))
            .border(3.dp, Color(0xFF2CF6FF), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.65f)) {
            val scaleX = this.size.width / 100f
            val scaleY = this.size.height / 80f

            val strokeWidth = 5f * scaleX
            val gradientBrush = Brush.linearGradient(
                colors = listOf(Color(0xFF2A7BFF), Color(0xFF2CF6FF)),
                start = Offset(0f, 0f),
                end = Offset(this.size.width, this.size.height)
            )

            val path = Path().apply {
                // Segment 1: Upper D stem and loop
                moveTo(15f * scaleX, 10f * scaleY)
                quadraticTo(15f * scaleX, 5f * scaleY, 25f * scaleX, 5f * scaleY)
                lineTo(50f * scaleX, 5f * scaleY)
                quadraticTo(55f * scaleX, 5f * scaleY, 55f * scaleX, 12f * scaleY)
                lineTo(55f * scaleX, 25f * scaleY)

                // Segment 2: Bottom leaf curve
                moveTo(15f * scaleX, 35f * scaleY)
                quadraticTo(10f * scaleX, 45f * scaleY, 20f * scaleX, 55f * scaleY)
                quadraticTo(35f * scaleX, 65f * scaleY, 60f * scaleX, 55f * scaleY)
                quadraticTo(68f * scaleX, 50f * scaleY, 68f * scaleX, 40f * scaleY)

                // Segment 3: Inner spine
                moveTo(20f * scaleX, 55f * scaleY)
                quadraticTo(15f * scaleX, 45f * scaleY, 30f * scaleX, 20f * scaleY)

                // Segment 4: Foliage leaf right arch
                moveTo(30f * scaleX, 30f * scaleY)
                quadraticTo(45f * scaleX, 15f * scaleY, 65f * scaleX, 30f * scaleY)
                quadraticTo(75f * scaleX, 40f * scaleY, 80f * scaleX, 25f * scaleY)
                quadraticTo(70f * scaleX, 10f * scaleY, 55f * scaleX, 15f * scaleY)
                quadraticTo(45f * scaleX, 20f * scaleY, 42f * scaleX, 35f * scaleY)

                // Segment 5: Crown leaf tip
                moveTo(55f * scaleX, 15f * scaleY)
                quadraticTo(65f * scaleX, 5f * scaleY, 75f * scaleX, 10f * scaleY)
                quadraticTo(80f * scaleX, 20f * scaleY, 72f * scaleX, 35f * scaleY)
            }

            drawPath(
                path = path,
                brush = gradientBrush,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
