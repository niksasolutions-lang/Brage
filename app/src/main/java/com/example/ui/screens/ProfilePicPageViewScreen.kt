package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.UserProfile
import com.example.ui.components.DeilaLogo

/**
 * Fullscreen dedicated "Profile Picture Page View" (View Profile Picture / Avatar Showcase).
 * Allows users to inspect their avatar in high resolution, zoom/pan, view badge details,
 * quickly switch to editing/changing, or share/view metadata.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePicPageViewScreen(
    userProfile: UserProfile,
    title: String = "Profile Picture",
    onEditPhotoClick: () -> Unit = {},
    onBackClick: () -> Unit
) {
    BackHandler {
        onBackClick()
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var showControls by remember { mutableStateOf(true) }
    var showInfoSheet by remember { mutableStateOf(false) }

    val isDeilaAi = userProfile.profilePhotoUri == "deila_logo_ai" ||
            userProfile.avatarPresetId == "preset_deila"
    val hasCustomPhoto = !userProfile.profilePhotoUri.isNullOrEmpty()

    val userInitials = remember(userProfile.fullName) {
        userProfile.fullName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .ifEmpty { "AM" }
    }

    Scaffold(
        containerColor = Color(0xFF030811),
        contentColor = Color.White,
        topBar = {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isDeilaAi) "Deila AI • Cybermonogram" else "@${userProfile.username}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2CF6FF)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("btn_back_profile_pic_view")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showInfoSheet = true },
                            modifier = Modifier.testTag("btn_info_profile_pic")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Picture Details",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = onEditPhotoClick,
                            modifier = Modifier.testTag("btn_edit_from_view_page")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile Picture",
                                tint = Color(0xFF2CF6FF)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF050D1A).copy(alpha = 0.85f)
                    )
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                Surface(
                    color = Color(0xFF050D1A).copy(alpha = 0.92f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    border = BorderStroke(1.dp, Color(0xFF2CF6FF).copy(alpha = 0.2f)),
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // User Info summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = userProfile.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isDeilaAi) "Official Deila AI Monogram" else "Current Profile Picture",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            if (scale != 1f || offset != Offset.Zero) {
                                TextButton(
                                    onClick = {
                                        scale = 1f
                                        offset = Offset.Zero
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = Color(0xFF2CF6FF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reset Zoom", color = Color(0xFF2CF6FF), fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: Edit / Change picture and Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showInfoSheet = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_view_details_sheet"),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Details", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = onEditPhotoClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_change_photo_action"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00E5FF)
                                )
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF030811), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Change Photo", color = Color(0xFF030811), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    showControls = !showControls
                },
            contentAlignment = Alignment.Center
        ) {
            // Subtle ambient neon watercolor backdrop
            Box(
                modifier = Modifier
                    .size(340.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00E5FF).copy(alpha = 0.15f),
                                Color(0xFF1A8CFF).copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Interactive Transformable Container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.8f, 4.0f)
                            offset = Offset(
                                x = offset.x + pan.x,
                                y = offset.y + pan.y
                            )
                        }
                    }
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
                contentAlignment = Alignment.Center
            ) {
                // Main Avatar Display Box
                if (isDeilaAi) {
                    // Full Glowing Deila Monogram with outer cyber glow ring
                    DeilaLogo(
                        size = 280.dp,
                        modifier = Modifier.testTag("fullscreen_deila_logo")
                    )
                } else if (hasCustomPhoto) {
                    // Custom photo in high resolution presentation
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color(0xFF2CF6FF), CircleShape)
                            .border(8.dp, Color(0xFF050D1A), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = userProfile.profilePhotoUri,
                            contentDescription = "User Profile Picture High Resolution",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    // Monogram / Avatar Preset Display
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .clip(CircleShape)
                            .background(Color(userProfile.avatarBgColorHex))
                            .border(4.dp, Color(0xFF2CF6FF), CircleShape)
                            .border(8.dp, Color(0xFF050D1A), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitials,
                            fontSize = 96.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            // Floating gesture instruction pill
            AnimatedVisibility(
                visible = showControls,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Surface(
                    color = Color(0xFF0F172A).copy(alpha = 0.8f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Pinch,
                            contentDescription = null,
                            tint = Color(0xFF2CF6FF),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pinch to zoom • Tap anywhere to toggle full view",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }

    // Picture Metadata BottomSheet Dialog
    if (showInfoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showInfoSheet = false },
            containerColor = Color(0xFF050D1A),
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "Profile Picture Info",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Format, styling & security specifications",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                HorizontalDivider(color = Color(0xFF1E293B))

                Spacer(modifier = Modifier.height(16.dp))

                InfoRow(
                    label = "Type",
                    value = if (isDeilaAi) "Deila AI Cybermonogram (Official)" else if (hasCustomPhoto) "Custom Image Upload" else "Initials Monogram"
                )
                InfoRow(
                    label = "Resolution",
                    value = if (isDeilaAi) "1024 x 1024 (Vector Rendered)" else "Full Resolution (Crop 1:1)"
                )
                InfoRow(
                    label = "Visible In",
                    value = "Service Bookings, Chat, Reviews & Concierge"
                )
                InfoRow(
                    label = "Identity Verification",
                    value = "Verified Member Profile"
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        showInfoSheet = false
                        onEditPhotoClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Text("Edit / Change This Picture", color = Color(0xFF030811), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}
