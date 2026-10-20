package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DownloadedVideo
import com.example.ui.components.DeilaVideoPlayer
import java.io.File

/**
 * DownloadedVideoPlayScreen - Dedicated offline video player screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadedVideoPlayScreen(
    video: DownloadedVideo,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(video.title, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        val file = File(video.local_path)
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (file.exists()) {
                DeilaVideoPlayer(
                    videoUri = Uri.fromFile(file),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "Video file not found locally",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}
