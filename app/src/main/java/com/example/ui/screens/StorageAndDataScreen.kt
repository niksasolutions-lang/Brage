package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageAndDataScreen(
    onBackClick: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var dataSaverEnabled by remember { mutableStateOf(false) }
    var useLessDataActive by remember { mutableStateOf(false) }
    var mediaUploadQuality by remember { mutableStateOf("High Quality") }
    var autoDownloadQuality by remember { mutableStateOf("Standard Quality") }
    var autoDownloadPhotos by remember { mutableStateOf(true) }
    var autoDownloadDocs by remember { mutableStateOf(false) }

    var cacheSizeMb by remember { mutableStateOf(14.8) }
    var dbSizeMb by remember { mutableStateOf(4.2) }
    var isClearingCache by remember { mutableStateOf(false) }
    var isClearingAllData by remember { mutableStateOf(false) }

    // Network stats
    var bytesSentMb by remember { mutableStateOf(18.4) }
    var bytesReceivedMb by remember { mutableStateOf(76.2) }

    // Proxy settings
    var proxyEnabled by remember { mutableStateOf(false) }
    var proxyHost by remember { mutableStateOf("192.168.1.100") }
    var proxyPort by remember { mutableStateOf("8080") }

    // Floating Dialog States
    var showManageStorageDialog by remember { mutableStateOf(false) }
    var showNetworkUsageDialog by remember { mutableStateOf(false) }
    var showProxyDialog by remember { mutableStateOf(false) }
    var showMediaQualityDialog by remember { mutableStateOf(false) }
    var showAutoDownloadDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Storage and Data", fontWeight = FontWeight.Bold)
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
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .testTag("storage_data_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
        ) {
            // Quick Action: Use Less Data Button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        useLessDataActive = !useLessDataActive
                        dataSaverEnabled = useLessDataActive
                        onShowToast(if (useLessDataActive) "Use Less Data mode activated (saved ~35% bandwidth)" else "Use Less Data mode deactivated")
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (useLessDataActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                Icons.Default.DataUsage,
                                contentDescription = null,
                                tint = if (useLessDataActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Use Less Data for Calls & Media",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (useLessDataActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (useLessDataActive) "Active • Reducing cellular usage" else "Tap to enable low-data mode",
                                    fontSize = 11.sp,
                                    color = if (useLessDataActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = useLessDataActive,
                            onCheckedChange = {
                                useLessDataActive = it
                                dataSaverEnabled = it
                                onShowToast(if (it) "Use Less Data activated" else "Use Less Data deactivated")
                            },
                            modifier = Modifier.testTag("switch_use_less_data")
                        )
                    }
                }
            }

            // Section 1: Manage Storage & Device Storage Breakdown
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manage Storage",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    TextButton(onClick = { showManageStorageDialog = true }) {
                        Text("View Details", fontSize = 12.sp)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showManageStorageDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Cached Media & Files", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Images, avatars, and profile previews", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(
                                text = "${String.format("%.1f", cacheSizeMb)} MB",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { (cacheSizeMb / 100.0).toFloat().coerceIn(0.0f, 1.0f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Dataset, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Local SQLite Database", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Bookings, reviews & local records", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(
                                text = "${String.format("%.1f", dbSizeMb)} MB",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (isClearingCache) {
                                Box(modifier = Modifier.fillMaxWidth().height(38.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        isClearingCache = true
                                        coroutineScope.launch {
                                            delay(1200)
                                            cacheSizeMb = 0.0
                                            isClearingCache = false
                                            onShowToast("Media cache cleared successfully!")
                                        }
                                    },
                                    modifier = Modifier.weight(1f).testTag("btn_clear_media_cache"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clear Cache", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        isClearingAllData = true
                                        coroutineScope.launch {
                                            delay(1500)
                                            cacheSizeMb = 0.0
                                            dbSizeMb = 0.0
                                            isClearingAllData = false
                                            onShowToast("All local app data reset successfully!")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                                    modifier = Modifier.weight(1f).testTag("btn_reset_all_data"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    if (isClearingAllData) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                                    } else {
                                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reset All Data", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Network Usage
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Network Usage",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    TextButton(onClick = { showNetworkUsageDialog = true }) {
                        Text("Detailed Stats", fontSize = 12.sp)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showNetworkUsageDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Upload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Data Sent", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Uploads, chat messages & telemetry", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(
                                text = "${String.format("%.1f", bytesSentMb)} MB",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Data Received", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Provider profiles, maps & live tracking", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(
                                text = "${String.format("%.1f", bytesReceivedMb)} MB",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Section 3: Floating Page Triggers (Media Upload Quality & Auto-Download Quality & Proxy)
            item {
                Text(
                    text = "Advanced Quality & Proxy",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Media Upload Quality Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showMediaQualityDialog = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.HighQuality, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Media Upload Quality", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Current: $mediaUploadQuality", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        // Auto-Download Quality Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAutoDownloadDialog = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.DownloadForOffline, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Auto-Download Quality", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Current: $autoDownloadQuality", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        // Proxy Page Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showProxyDialog = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Network Proxy Settings", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(if (proxyEnabled) "Active ($proxyHost:$proxyPort)" else "Disabled", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    // Floating Dialog: Manage Storage
    if (showManageStorageDialog) {
        AlertDialog(
            onDismissRequest = { showManageStorageDialog = false },
            title = { Text("Manage Storage Breakdown") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Here you can inspect and manage your local storage usage across categories.", fontSize = 13.sp)
                    Divider()
                    Text("• Profile Images Cache: ${String.format("%.1f", cacheSizeMb)} MB", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("• Local Bookings DB: ${String.format("%.1f", dbSizeMb)} MB", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("• App Binary & Assets: 32.5 MB", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Total Available Space: 128 GB (108 GB Free)", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            },
            confirmButton = {
                TextButton(onClick = { showManageStorageDialog = false }) {
                    Text("Close")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    cacheSizeMb = 0.0
                    dbSizeMb = 0.0
                    showManageStorageDialog = false
                    onShowToast("All storage cleared!")
                }) {
                    Text("Clear All")
                }
            }
        )
    }

    // Floating Dialog: Network Usage
    if (showNetworkUsageDialog) {
        AlertDialog(
            onDismissRequest = { showNetworkUsageDialog = false },
            title = { Text("Detailed Network Usage") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Lifetime data statistics since last reset:", fontSize = 13.sp)
                    Divider()
                    Text("• Sent: ${String.format("%.1f", bytesSentMb)} MB", fontSize = 12.sp)
                    Text("• Received: ${String.format("%.1f", bytesReceivedMb)} MB", fontSize = 12.sp)
                    Text("• Total Bandwidth: ${String.format("%.1f", bytesSentMb + bytesReceivedMb)} MB", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("• Active Connections: 3 Secure Sockets", fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showNetworkUsageDialog = false }) {
                    Text("Done")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    bytesSentMb = 0.0
                    bytesReceivedMb = 0.0
                    showNetworkUsageDialog = false
                    onShowToast("Network statistics reset!")
                }) {
                    Text("Reset Stats")
                }
            }
        )
    }

    // Floating Dialog: Media Upload Quality
    if (showMediaQualityDialog) {
        AlertDialog(
            onDismissRequest = { showMediaQualityDialog = false },
            title = { Text("Media Upload Quality") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select default quality for uploading portfolio photos, work receipts, and chat attachments:", fontSize = 13.sp)
                    
                    listOf("Data Saver (Low Res)", "Standard Quality", "High Quality (HD)").forEach { quality ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    mediaUploadQuality = quality
                                    showMediaQualityDialog = false
                                    onShowToast("Upload quality set to $quality")
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = mediaUploadQuality == quality,
                                onClick = {
                                    mediaUploadQuality = quality
                                    showMediaQualityDialog = false
                                    onShowToast("Upload quality set to $quality")
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(quality, fontSize = 14.sp, fontWeight = if (mediaUploadQuality == quality) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMediaQualityDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Floating Dialog: Auto-Download Quality
    if (showAutoDownloadDialog) {
        AlertDialog(
            onDismissRequest = { showAutoDownloadDialog = false },
            title = { Text("Auto-Download Quality") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Choose auto-download preference for images, audio messages, and documents over Wi-Fi:", fontSize = 13.sp)

                    listOf("Off", "Low Quality", "Standard Quality", "Original Quality").forEach { quality ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    autoDownloadQuality = quality
                                    showAutoDownloadDialog = false
                                    onShowToast("Auto-download quality set to $quality")
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = autoDownloadQuality == quality,
                                onClick = {
                                    autoDownloadQuality = quality
                                    showAutoDownloadDialog = false
                                    onShowToast("Auto-download quality set to $quality")
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(quality, fontSize = 14.sp, fontWeight = if (autoDownloadQuality == quality) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAutoDownloadDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Floating Dialog: Proxy Page
    if (showProxyDialog) {
        AlertDialog(
            onDismissRequest = { showProxyDialog = false },
            title = { Text("Network Proxy Configuration") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Configure HTTP/SOCKS proxy server routing for enterprise or restricted networks.", fontSize = 13.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Proxy", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Switch(
                            checked = proxyEnabled,
                            onCheckedChange = { proxyEnabled = it }
                        )
                    }

                    OutlinedTextField(
                        value = proxyHost,
                        onValueChange = { proxyHost = it },
                        label = { Text("Proxy Host / IP") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = proxyPort,
                        onValueChange = { proxyPort = it },
                        label = { Text("Port") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showProxyDialog = false
                        onShowToast(if (proxyEnabled) "Proxy connected to $proxyHost:$proxyPort" else "Proxy disabled")
                    }
                ) {
                    Text("Save & Connect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProxyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
