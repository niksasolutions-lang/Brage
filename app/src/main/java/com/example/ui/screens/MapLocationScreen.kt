package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProviderInfo
import com.example.data.model.ServiceCategory
import com.example.data.model.ServiceItem
import com.example.data.model.ServiceLocationInfo
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class PresetLocation(
    val name: String,
    val fullAddress: String,
    val lat: Double,
    val lng: Double,
    val zip: String
)

val PRESET_LOCATIONS = listOf(
    PresetLocation("Springfield Downtown", "555 Main St, Springfield, OR 97477", 44.0462, -123.0220, "97477"),
    PresetLocation("Gateway Center", "3000 Gateway St, Springfield, OR 97477", 44.0734, -123.0489, "97477"),
    PresetLocation("Thurston District", "5700 Main St, Springfield, OR 97478", 44.0498, -122.9234, "97478"),
    PresetLocation("Eugene North / Coburg", "1000 Coburg Rd, Eugene, OR 97401", 44.0722, -123.0845, "97401"),
    PresetLocation("Eugene Downtown", "800 Willamette St, Eugene, OR 97401", 44.0505, -123.0926, "97401"),
    PresetLocation("River Road Area", "2100 River Rd, Eugene, OR 97404", 44.0920, -123.1310, "97404")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapLocationScreen(
    currentLocation: ServiceLocationInfo,
    categories: List<ServiceCategory>,
    allServices: List<ServiceItem>,
    initialSelectedProviderId: String? = null,
    onBackClick: () -> Unit,
    onSaveLocation: (ServiceLocationInfo) -> Unit,
    onViewProviderProfile: (String) -> Unit,
    onBookService: (ServiceItem) -> Unit,
    onCallProvider: (name: String, role: String, initials: String, contextTitle: String, phone: String) -> Unit
) {
    BackHandler {
        onBackClick()
    }

    var searchAddress by remember { mutableStateOf(currentLocation.address) }
    var selectedLocationName by remember { mutableStateOf(currentLocation.cityState) }
    var userLat by remember { mutableDoubleStateOf(currentLocation.latitude) }
    var userLng by remember { mutableDoubleStateOf(currentLocation.longitude) }

    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) } // null = all
    var serviceRadiusMiles by remember { mutableFloatStateOf(15f) }
    var selectedProviderId by remember { mutableStateOf(initialSelectedProviderId) }

    var mapOffsetX by remember { mutableFloatStateOf(0f) }
    var mapOffsetY by remember { mutableFloatStateOf(0f) }
    var isMapInteracting by remember { mutableStateOf(false) }
    var showLocationSuccessToast by remember { mutableStateOf(false) }

    // Distinct list of providers
    val uniqueProviders = remember(allServices) {
        allServices.map { it.provider }.distinctBy { it.id }
    }

    // Filtered providers based on category & radius
    val filteredProviders = remember(uniqueProviders, selectedCategoryFilter, serviceRadiusMiles, userLat, userLng) {
        uniqueProviders.filter { provider ->
            val matchingServices = allServices.filter { it.provider.id == provider.id }
            val matchesCategory = selectedCategoryFilter == null || matchingServices.any { it.categoryId == selectedCategoryFilter }
            val distance = calculateDistance(userLat, userLng, provider.latitude, provider.longitude)
            matchesCategory && distance <= serviceRadiusMiles
        }
    }

    val selectedProvider = remember(selectedProviderId, uniqueProviders) {
        uniqueProviders.find { it.id == selectedProviderId }
    }

    val selectedProviderServices = remember(selectedProviderId, allServices) {
        allServices.filter { it.provider.id == selectedProviderId }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Service Location & Live Map",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Set your dispatch address & see verified specialists",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = {
                                // Reset to GPS Springfield
                                userLat = 44.0462
                                userLng = -123.0220
                                searchAddress = "555 Main St, Springfield, OR 97477"
                                selectedLocationName = "Springfield, OR"
                                mapOffsetX = 0f
                                mapOffsetY = 0f
                            },
                            modifier = Modifier.testTag("map_gps_center_button")
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Locate Me", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Address Search & Picker Field
                    OutlinedTextField(
                        value = searchAddress,
                        onValueChange = {
                            searchAddress = it
                            selectedLocationName = it.split(",").firstOrNull()?.trim() ?: it
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            if (searchAddress.isNotBlank()) {
                                IconButton(onClick = { searchAddress = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        placeholder = { Text("Search address, landmark, or zip...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("map_address_search_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Location Preset Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(PRESET_LOCATIONS) { preset ->
                            val isSelected = selectedLocationName.contains(preset.name.split(" ").first(), ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    userLat = preset.lat
                                    userLng = preset.lng
                                    searchAddress = preset.fullAddress
                                    selectedLocationName = preset.name
                                    mapOffsetX = 0f
                                    mapOffsetY = 0f
                                },
                                label = {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Radius Slider & Pro Count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NearMe, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Search Radius: ${serviceRadiusMiles.toInt()} miles",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${filteredProviders.size} Verified Pros Found",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Slider(
                        value = serviceRadiusMiles,
                        onValueChange = { serviceRadiusMiles = it },
                        valueRange = 3f..30f,
                        steps = 5,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Confirm Set Location Button
                    Button(
                        onClick = {
                            val newLoc = ServiceLocationInfo(
                                cityState = selectedLocationName.ifBlank { "Springfield, OR" },
                                address = searchAddress.ifBlank { "555 Main St, Springfield, OR 97477" },
                                latitude = userLat,
                                longitude = userLng
                            )
                            onSaveLocation(newLoc)
                            showLocationSuccessToast = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("confirm_set_location_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Set as Active Service Location",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive Map Canvas
            MapCanvasView(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isMapInteracting = true },
                            onDragEnd = { isMapInteracting = false },
                            onDragCancel = { isMapInteracting = false },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                mapOffsetX += dragAmount.x
                                mapOffsetY += dragAmount.y
                                // Subtle coordinate drift based on map dragging
                                userLat -= dragAmount.y * 0.00005
                                userLng += dragAmount.x * 0.00005
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { tapOffset ->
                            // Tap on map updates pin location
                            selectedProviderId = null
                        }
                    },
                userLat = userLat,
                userLng = userLng,
                radiusMiles = serviceRadiusMiles,
                providers = filteredProviders,
                selectedProviderId = selectedProviderId,
                mapOffsetX = mapOffsetX,
                mapOffsetY = mapOffsetY,
                onSelectProvider = { providerId ->
                    selectedProviderId = providerId
                }
            )

            // Category Filter Pills overlay at the top of the map
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("All (${uniqueProviders.size})", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            )
                        )
                    }

                    items(categories) { cat ->
                        val isSelected = selectedCategoryFilter == cat.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategoryFilter = if (isSelected) null else cat.id
                            },
                            label = { Text(cat.title, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            )
                        )
                    }
                }
            }

            // Interactive Provider Bottom Detail Card (When a provider marker is selected)
            selectedProvider?.let { provider ->
                val matchingService = selectedProviderServices.firstOrNull() ?: allServices.first()
                val distance = calculateDistance(userLat, userLng, provider.latitude, provider.longitude)

                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .shadow(12.dp, RoundedCornerShape(20.dp))
                        .testTag("map_provider_detail_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = provider.initials,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = provider.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            Icons.Default.Verified,
                                            contentDescription = "Verified",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = provider.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            IconButton(onClick = { selectedProviderId = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Distance, Rating, and Dispatch highlights
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Navigation, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${String.format(java.util.Locale.US, "%.1f", distance)} mi away",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF3C7),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${provider.rating} (${provider.reviewCount})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFD1FAE5),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = provider.responseTimeText,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF065F46)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onViewProviderProfile(provider.id) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Full Profile", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onBookService(matchingService) },
                                modifier = Modifier.weight(1.3f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Book Direct", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Success Confirmation Snack/Dialog
            if (showLocationSuccessToast) {
                AlertDialog(
                    onDismissRequest = { showLocationSuccessToast = false },
                    icon = {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                    },
                    title = { Text("Service Location Updated!", fontWeight = FontWeight.Bold) },
                    text = {
                        Text("Active service address set to:\n${searchAddress}\n\nNearby verified specialists and instant booking estimates have been refreshed for your area.")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showLocationSuccessToast = false
                                onBackClick()
                            }
                        ) {
                            Text("Done • Explore Services")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MapCanvasView(
    modifier: Modifier = Modifier,
    userLat: Double,
    userLng: Double,
    radiusMiles: Float,
    providers: List<ProviderInfo>,
    selectedProviderId: String?,
    mapOffsetX: Float,
    mapOffsetY: Float,
    onSelectProvider: (String) -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val centerX = width / 2f + mapOffsetX
        val centerY = height / 2f + mapOffsetY

        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. Background Map Color (Terrain / City style)
            drawRect(Color(0xFFE5E7EB))

            // 2. Parks and Green Spaces
            val parkPath = Path().apply {
                moveTo(centerX - 350f, centerY - 280f)
                lineTo(centerX - 180f, centerY - 320f)
                lineTo(centerX - 120f, centerY - 190f)
                lineTo(centerX - 290f, centerY - 150f)
                close()

                moveTo(centerX + 150f, centerY + 180f)
                lineTo(centerX + 320f, centerY + 120f)
                lineTo(centerX + 380f, centerY + 260f)
                lineTo(centerX + 210f, centerY + 310f)
                close()
            }
            drawPath(parkPath, Color(0xFFD1FAE5))

            // 3. River / Waterway (Willamette / McKenzie river style)
            val riverPath = Path().apply {
                moveTo(0f, centerY - 120f)
                cubicTo(
                    centerX - 150f, centerY - 80f,
                    centerX + 80f, centerY - 200f,
                    width, centerY - 160f
                )
            }
            drawPath(
                riverPath,
                Color(0xFFBFDBFE),
                style = Stroke(width = 32f)
            )

            // 4. City Road Grids & Highways
            val roadColor = Color.White
            val highwayColor = Color(0xFFFDE68A)

            // Horizontal major roads
            for (i in -4..4) {
                val y = centerY + i * 110f
                drawLine(
                    color = if (i == 0) highwayColor else roadColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = if (i == 0) 14f else 8f
                )
            }

            // Vertical major roads
            for (i in -4..4) {
                val x = centerX + i * 110f
                drawLine(
                    color = if (i == 0) highwayColor else roadColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = if (i == 0) 14f else 8f
                )
            }

            // Diagonal avenues
            drawLine(
                color = roadColor,
                start = Offset(0f, centerY + 200f),
                end = Offset(width, centerY - 200f),
                strokeWidth = 6f
            )

            // 5. Radius Coverage Circle around User Location
            val radiusPx = radiusMiles * 18f
            drawCircle(
                color = Color(0xFF2563EB).copy(alpha = 0.08f),
                radius = radiusPx,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color(0xFF2563EB).copy(alpha = 0.4f),
                radius = radiusPx,
                center = Offset(centerX, centerY),
                style = Stroke(width = 2f)
            )

            // 6. User Service Location Pin (Center Crosshair & Marker)
            drawCircle(
                color = Color(0xFF2563EB).copy(alpha = 0.25f),
                radius = 24f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color(0xFF2563EB),
                radius = 12f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color.White,
                radius = 5f,
                center = Offset(centerX, centerY)
            )
        }

        // 7. Interactive Provider Markers placed on the map
        providers.forEach { provider ->
            // Calculate screen position offset relative to user center
            val dLat = (provider.latitude - userLat) * 11000f
            val dLng = (provider.longitude - userLng) * 11000f

            val posX = centerX + dLng.toFloat()
            val posY = centerY - dLat.toFloat()

            val isSelected = provider.id == selectedProviderId

            Box(
                modifier = Modifier
                    .offset(
                        x = (posX - 24f).dp / (LocalDensityWrapper.getDensity()),
                        y = (posY - 52f).dp / (LocalDensityWrapper.getDensity())
                    )
                    .clickable { onSelectProvider(provider.id) }
                    .testTag("map_marker_${provider.id}"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Marker Callout Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFF1E1B4B) else Color.White,
                        shadowElevation = if (isSelected) 10.dp else 4.dp,
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFBBF24))
                        } else {
                            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFFFBBF24) else Color(0xFF2563EB),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = provider.name.split(" ").first(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) Color.White else Color(0xFF0F172A),
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(10.dp))
                                    Text(
                                        text = "${provider.rating}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                        }
                    }

                    // Marker Pin Stem
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = if (isSelected) Color(0xFF1E1B4B) else Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .offset(y = (-4).dp)
                    )
                }
            }
        }
    }
}

object LocalDensityWrapper {
    @Composable
    fun getDensity(): Float {
        return androidx.compose.ui.platform.LocalDensity.current.density
    }
}

private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 3958.8 // Radius of the earth in miles
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * Math.atan2(sqrt(a), sqrt(1 - a))
    return r * c
}
