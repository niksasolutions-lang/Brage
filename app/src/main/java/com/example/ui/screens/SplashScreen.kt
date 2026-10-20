package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDismiss: () -> Unit) {
    var timeLeft by remember { mutableStateOf(5) }
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 5000, easing = LinearEasing),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        repeat(5) {
            delay(1000)
            timeLeft--
        }
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0a5bd7), Color(0xFF083a9e))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White, RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🏠", fontSize = 50.sp)
            }
            Text(
                text = "BRAGE",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 18.dp)
            )
            Text(
                text = "DEILA PLAY • On-Demand Home Services",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
            
            Box(
                modifier = Modifier
                    .padding(top = 45.dp)
                    .width(170.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.25f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progress)
                        .background(Color.White, RoundedCornerShape(20.dp))
                )
            }
            
            Text(
                text = "$timeLeft",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        
        Text(
            text = "Trusted • Reliable • Eldoret, Kenya",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 22.dp)
        )
    }
}
