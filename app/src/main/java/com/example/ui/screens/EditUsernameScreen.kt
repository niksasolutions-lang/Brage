package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUsernameScreen(
    currentProfile: UserProfile,
    onSaveProfileInfo: (fullName: String, username: String, bio: String) -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler {
        onBackClick()
    }

    var fullNameInput by remember { mutableStateOf(currentProfile.fullName) }
    var usernameInput by remember { mutableStateOf(currentProfile.username) }
    var bioInput by remember { mutableStateOf(currentProfile.bio) }

    // 60-day Lockout Check
    val daysRemaining = remember(currentProfile.usernameLastUpdatedMillis) {
        val sixtyDaysMillis = 60L * 24L * 60L * 60L * 1000L
        val elapsed = System.currentTimeMillis() - currentProfile.usernameLastUpdatedMillis
        if (currentProfile.usernameLastUpdatedMillis > 0L && elapsed < sixtyDaysMillis) {
            val remainingMs = sixtyDaysMillis - elapsed
            val remainingDays = remainingMs / (24L * 60L * 60L * 1000L)
            remainingDays.coerceAtLeast(1)
        } else {
            0L
        }
    }
    val isUsernameLocked = daysRemaining > 0L

    // Validation
    val isUsernameValid = remember(usernameInput) {
        val regex = "^[a-zA-Z0-9_]{3,20}$".toRegex()
        usernameInput.matches(regex)
    }

    val isFullNameValid = remember(fullNameInput) {
        fullNameInput.trim().length >= 2
    }

    // Only allow saving if full name is valid, and either username is not locked and is valid, OR username is locked (meaning it won't change)
    val canSave = isFullNameValid && (isUsernameLocked || isUsernameValid)

    val initials = remember(fullNameInput) {
        fullNameInput.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .ifEmpty { "AM" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Username & Display Name", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                            if (canSave) {
                                val finalUsername = if (isUsernameLocked) currentProfile.username else usernameInput.trim().lowercase()
                                onSaveProfileInfo(fullNameInput.trim(), finalUsername, bioInput.trim())
                            }
                        },
                        enabled = canSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_save_username"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            // Live Preview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Live Identity Preview",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(currentProfile.avatarBgColorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = fullNameInput.ifBlank { "Your Name" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "@${usernameInput.ifBlank { "username" }}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Verified Member • ${currentProfile.memberSince}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (bioInput.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "\"${bioInput.trim()}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Input Fields Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Account Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Full Name Input
                        OutlinedTextField(
                            value = fullNameInput,
                            onValueChange = { fullNameInput = it },
                            label = { Text("Full Legal / Display Name") },
                            placeholder = { Text("e.g. Alex Morgan") },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_full_name"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            isError = !isFullNameValid && fullNameInput.isNotBlank(),
                            supportingText = {
                                if (!isFullNameValid && fullNameInput.isNotBlank()) {
                                    Text("Please enter at least 2 characters", color = MaterialTheme.colorScheme.error)
                                } else {
                                    Text("Appears on booking tickets, invoices, and service dispatch notes")
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                keyboardType = KeyboardType.Text
                            )
                        )

                        // Username Handle Input
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { input ->
                                if (!isUsernameLocked) {
                                    // Filter to valid characters
                                    usernameInput = input.filter { it.isLetterOrDigit() || it == '_' }
                                }
                            },
                            enabled = !isUsernameLocked,
                            label = { Text("Unique Username (@handle)") },
                            placeholder = { Text("alex_morgan") },
                            leadingIcon = {
                                Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = if (isUsernameLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary)
                            },
                            trailingIcon = {
                                if (isUsernameLocked) {
                                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFF59E0B))
                                } else if (isUsernameValid) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Valid", tint = Color(0xFF10B981))
                                } else if (usernameInput.isNotBlank()) {
                                    Icon(Icons.Default.Error, contentDescription = "Invalid", tint = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_username"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            isError = !isUsernameLocked && !isUsernameValid && usernameInput.isNotBlank(),
                            supportingText = {
                                when {
                                    isUsernameLocked -> {
                                        Text(
                                            text = "⚠️ Username locked for another $daysRemaining days (can only change once every 60 days)",
                                            color = Color(0xFFD97706),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    !isUsernameValid && usernameInput.isNotBlank() -> {
                                        Text("Must be 3-20 characters: letters, numbers, or underscores only", color = MaterialTheme.colorScheme.error)
                                    }
                                    isUsernameValid -> {
                                        Text("Username handle is valid & available ✓", color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                                    }
                                    else -> {
                                        Text("Unique identifier for ratings, reviews, and provider communications")
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.None,
                                keyboardType = KeyboardType.Ascii
                            )
                        )

                        // Bio / Notes Input
                        OutlinedTextField(
                            value = bioInput,
                            onValueChange = { bioInput = it },
                            label = { Text("Bio & Homeowner Notes (Optional)") },
                            placeholder = { Text("e.g. Homeowner in Springfield. Seeking reliable home maintenance.") },
                            leadingIcon = {
                                Icon(Icons.Default.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_bio"),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 3,
                            supportingText = {
                                Text("Shared with service pros when you book an appointment")
                            }
                        )
                    }
                }
            }

            // Read-Only Contact Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Linked Credentials",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Email Address", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(currentProfile.email, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF059669).copy(alpha = 0.15f)
                            ) {
                                Text("VERIFIED", color = Color(0xFF059669), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Phone Number", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(currentProfile.phone, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF059669).copy(alpha = 0.15f)
                            ) {
                                Text("SMS READY", color = Color(0xFF059669), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
