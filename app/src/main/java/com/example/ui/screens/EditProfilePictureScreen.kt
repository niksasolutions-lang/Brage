package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AvatarPreset(
    val id: String,
    val name: String,
    val iconName: String,
    val bgColors: List<Color>
)

data class MockFileItem(
    val name: String,
    val size: String,
    val date: String,
    val imageUrl: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfilePictureScreen(
    currentProfile: UserProfile,
    onSaveProfilePicture: (photoUri: String?, presetId: String, bgColorHex: Long) -> Unit,
    onViewFullScreenClick: (() -> Unit)? = null,
    onBackClick: () -> Unit
) {
    BackHandler {
        onBackClick()
    }

    val coroutineScope = rememberCoroutineScope()

    var selectedPhotoUri by remember { mutableStateOf<String?>(currentProfile.profilePhotoUri) }
    var selectedPresetId by remember { mutableStateOf(currentProfile.avatarPresetId) }
    var selectedColorHex by remember { mutableStateOf(currentProfile.avatarBgColorHex) }
    var isSavedToastVisible by remember { mutableStateOf(false) }

    // Mock Files State
    var showFilePickerDialog by remember { mutableStateOf(false) }
    var isSimulatingFileLoading by remember { mutableStateOf(false) }
    var loadedFileName by remember { mutableStateOf("") }

    val mockFiles = remember {
        listOf(
            MockFileItem(
                name = "deila-logo.png",
                size = "420 KB",
                date = "Just now",
                imageUrl = "deila_logo_ai",
                description = "DEILA • Official AI Avatar & Cybermonogram"
            ),
            MockFileItem(
                name = "IMG_20261007_0940.jpg",
                size = "1.8 MB",
                date = "Today, 09:40 AM",
                imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=250&q=80",
                description = "Camera • High Resolution Portrait"
            ),
            MockFileItem(
                name = "professional_headshot_2026.png",
                size = "840 KB",
                date = "Oct 4, 2026",
                imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=250&q=80",
                description = "Downloads • Cropped PNG format"
            ),
            MockFileItem(
                name = "brage_id_selfie_retro.jpg",
                size = "2.2 MB",
                date = "Sep 28, 2026",
                imageUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=250&q=80",
                description = "Personal Vault • Verification face lock"
            ),
            MockFileItem(
                name = "creative_custom_profile.png",
                size = "1.1 MB",
                date = "Sep 15, 2026",
                imageUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=250&q=80",
                description = "Pictures • Bright gradient profile image"
            )
        )
    }

    // Android Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri.toString()
            selectedPresetId = "custom_upload"
        }
    }

    val avatarPresets = listOf(
        AvatarPreset("preset_deila", "Deila AI", "Deila", listOf(Color(0xFF00E5FF), Color(0xFF2A7BFF), Color(0xFF050D1A))),
        AvatarPreset("preset_default", "Standard", "Person", listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))),
        AvatarPreset("preset_star", "VIP Star", "Star", listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
        AvatarPreset("preset_shield", "Protected", "Shield", listOf(Color(0xFF10B981), Color(0xFF059669))),
        AvatarPreset("preset_eco", "Eco Home", "Eco", listOf(Color(0xFF059669), Color(0xFF047857))),
        AvatarPreset("preset_tech", "Smart Home", "ElectricBolt", listOf(Color(0xFF6366F1), Color(0xFF4F46E5))),
        AvatarPreset("preset_builder", "Pro Builder", "Build", listOf(Color(0xFFEA580C), Color(0xFFC2410C))),
        AvatarPreset("preset_vip", "Crown Elite", "WorkspacePremium", listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)))
    )

    val colorPalette = listOf(
        0xFF2563EB to "Royal Blue",
        0xFF059669 to "Emerald Green",
        0xFF7C3AED to "Violet Purple",
        0xFFD97706 to "Amber Gold",
        0xFFDB2777 to "Rose Pink",
        0xFF0284C7 to "Sky Cyan",
        0xFF0F172A to "Slate Dark"
    )

    val userInitials = remember(currentProfile.fullName) {
        currentProfile.fullName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .ifEmpty { "AM" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Profile Picture & Avatar", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (onViewFullScreenClick != null) {
                        IconButton(
                            onClick = onViewFullScreenClick,
                            modifier = Modifier.testTag("btn_top_view_fullscreen")
                        ) {
                            Icon(
                                Icons.Default.Fullscreen,
                                contentDescription = "View Picture Fullscreen",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            onSaveProfilePicture(selectedPhotoUri, selectedPresetId, selectedColorHex)
                            isSavedToastVisible = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_save_profile_pic"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Profile Picture", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
        ) {
            // Live Avatar Preview Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color(selectedColorHex))
                                    .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!selectedPhotoUri.isNullOrEmpty()) {
                                    if (selectedPhotoUri == "deila_logo_ai") {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_deila_avatar),
                                            contentDescription = "Deila AI Logo Profile",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        AsyncImage(
                                            model = selectedPhotoUri,
                                            contentDescription = "Profile Photo",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                } else {
                                    val matchedPreset = avatarPresets.find { it.id == selectedPresetId }
                                    if (matchedPreset != null && matchedPreset.id == "preset_deila") {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_deila_avatar),
                                            contentDescription = "Deila AI Avatar",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else if (matchedPreset != null && matchedPreset.id != "preset_default") {
                                        Icon(
                                            imageVector = when (matchedPreset.iconName) {
                                                "Star" -> Icons.Default.Star
                                                "Shield" -> Icons.Default.Shield
                                                "Eco" -> Icons.Default.Eco
                                                "ElectricBolt" -> Icons.Default.ElectricBolt
                                                "Build" -> Icons.Default.Build
                                                "WorkspacePremium" -> Icons.Default.WorkspacePremium
                                                else -> Icons.Default.Person
                                            },
                                            contentDescription = matchedPreset.name,
                                            tint = Color.White,
                                            modifier = Modifier.size(54.dp)
                                        )
                                    } else {
                                        Text(
                                            text = userInitials,
                                            style = MaterialTheme.typography.displaySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // Camera / Edit badge (Opens options dialog sheet)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                    .clickable {
                                        showFilePickerDialog = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Upload Options",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = currentProfile.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "@${currentProfile.username} • Verified Member",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (onViewFullScreenClick != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            FilledTonalButton(
                                onClick = onViewFullScreenClick,
                                modifier = Modifier
                                    .testTag("btn_view_full_pic"),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("View Profile Picture Fullscreen", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Quick Photo Actions (Adding direct simulated files option)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_choose_gallery_photo"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Photo Picker", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { showFilePickerDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_choose_files_photo"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Choose Files", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    if (selectedPhotoUri != null) {
                        FilledTonalButton(
                            onClick = {
                                selectedPhotoUri = null
                                selectedPresetId = "preset_default"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_remove_photo"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Remove Custom Photo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Avatar Style Presets Section
            item {
                Column {
                    Text(
                        text = "Or Choose an Avatar Style",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Select an illustrated badge that displays across bookings and support",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(avatarPresets) { preset ->
                            val isSelected = selectedPresetId == preset.id && selectedPhotoUri == null
                            Card(
                                modifier = Modifier
                                    .width(88.dp)
                                    .clickable {
                                        selectedPhotoUri = null
                                        selectedPresetId = preset.id
                                    }
                                    .testTag("avatar_preset_${preset.id}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                ),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(preset.bgColors)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (preset.id == "preset_deila") {
                                            Image(
                                                painter = painterResource(id = R.drawable.img_deila_avatar),
                                                contentDescription = "Deila AI",
                                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else if (preset.id == "preset_default") {
                                            Text(
                                                text = userInitials,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 16.sp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = when (preset.iconName) {
                                                    "Star" -> Icons.Default.Star
                                                    "Shield" -> Icons.Default.Shield
                                                    "Eco" -> Icons.Default.Eco
                                                    "ElectricBolt" -> Icons.Default.ElectricBolt
                                                    "Build" -> Icons.Default.Build
                                                    "WorkspacePremium" -> Icons.Default.WorkspacePremium
                                                    else -> Icons.Default.Person
                                                },
                                                contentDescription = preset.name,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Monogram Background Color Picker
            item {
                Column {
                    Text(
                        text = "Monogram & Avatar Accent Color",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Personalize your monogram backdrop color",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(colorPalette) { (colorHex, name) ->
                            val isSelected = selectedColorHex == colorHex
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { selectedColorHex = colorHex }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(colorHex))
                                        .border(
                                            if (isSelected) 3.dp else 1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = name.split(" ").first(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Interactive "Choose from Files / Device Storage" Simulation Dialog ---
    if (showFilePickerDialog) {
        var activeTab by remember { mutableStateOf("Downloads") }

        AlertDialog(
            onDismissRequest = { showFilePickerDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Device Files Picker", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { showFilePickerDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Choose an image file from local folders or shared external storage:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Folder selection tabs
                    ScrollableTabRow(
                        selectedTabIndex = when (activeTab) {
                            "Downloads" -> 0
                            "Camera" -> 1
                            "Screenshots" -> 2
                            else -> 3
                        },
                        containerColor = Color.Transparent,
                        edgePadding = 0.dp,
                        divider = {}
                    ) {
                        Tab(selected = activeTab == "Downloads", onClick = { activeTab = "Downloads" }) {
                            Text("Downloads", modifier = Modifier.padding(vertical = 10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Tab(selected = activeTab == "Camera", onClick = { activeTab = "Camera" }) {
                            Text("Camera", modifier = Modifier.padding(vertical = 10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Tab(selected = activeTab == "Screenshots", onClick = { activeTab = "Screenshots" }) {
                            Text("Screenshots", modifier = Modifier.padding(vertical = 10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Tab(selected = activeTab == "Drive", onClick = { activeTab = "Drive" }) {
                            Text("Google Drive", modifier = Modifier.padding(vertical = 10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    if (isSimulatingFileLoading) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading visual assets of '$loadedFileName'...",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val filteredFiles = when (activeTab) {
                                "Camera" -> mockFiles.filter { it.name.contains("IMG_") }
                                "Downloads" -> mockFiles.filter { it.name.contains("headshot") }
                                "Screenshots" -> mockFiles.filter { it.name.contains("creative") }
                                else -> mockFiles.filter { it.name.contains("brage") }
                            }.ifEmpty { mockFiles }

                            items(filteredFiles) { file ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            loadedFileName = file.name
                                            isSimulatingFileLoading = true
                                            coroutineScope.launch {
                                                delay(1200)
                                                isSimulatingFileLoading = false
                                                selectedPhotoUri = file.imageUrl
                                                selectedPresetId = "custom_upload"
                                                showFilePickerDialog = false
                                            }
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Miniature image preview box
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        ) {
                                            if (file.imageUrl == "deila_logo_ai") {
                                                Image(
                                                    painter = painterResource(id = R.drawable.img_deila_avatar),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                AsyncImage(
                                                    model = file.imageUrl,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = file.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                            Text(text = "${file.size} • ${file.date}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = file.description, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                                        }

                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFilePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
