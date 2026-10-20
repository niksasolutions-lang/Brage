package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ServiceItem
import com.example.ui.components.ServiceCard

@Composable
fun SavedScreen(
    savedServices: List<ServiceItem>,
    onFavoriteToggle: (String) -> Unit,
    onServiceClick: (ServiceItem) -> Unit,
    onBookServiceClick: (ServiceItem) -> Unit,
    onCallProvider: (name: String, role: String, initials: String, contextTitle: String, phone: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("saved_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Saved Services & Pros",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Quick access to your go-to specialists for repeat bookings",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (savedServices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No saved services yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap the heart icon on any service card to bookmark it here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(savedServices) { service ->
                    ServiceCard(
                        service = service,
                        isFavorite = true,
                        onFavoriteToggle = { onFavoriteToggle(service.id) },
                        onClick = { onServiceClick(service) },
                        onBookClick = { onBookServiceClick(service) },
                        onCallClick = {
                            onCallProvider(
                                service.provider.name,
                                service.provider.title,
                                service.provider.initials,
                                service.title,
                                "+1 (555) 382-9912"
                            )
                        }
                    )
                }
            }
        }
    }
}
