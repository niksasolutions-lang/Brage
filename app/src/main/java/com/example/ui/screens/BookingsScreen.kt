package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BookingEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    bookings: List<BookingEntity>,
    onAdvanceStatus: (BookingEntity) -> Unit,
    onCancelBooking: (Long) -> Unit,
    onOpenReschedule: (BookingEntity) -> Unit,
    onOpenReview: (BookingEntity) -> Unit,
    onCallPro: (name: String, role: String, initials: String, contextTitle: String, phone: String) -> Unit,
    onChatPro: (name: String, role: String, initials: String, contextTitle: String, phone: String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val activeBookings = bookings.filter { it.status != "COMPLETED" && it.status != "CANCELLED" }
    val pastBookings = bookings.filter { it.status == "COMPLETED" || it.status == "CANCELLED" }

    val displayedList = if (selectedTab == 0) activeBookings else pastBookings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("bookings_screen")
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Text(
                text = "My Bookings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Track ongoing appointments and past service history",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Active (${activeBookings.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "History (${pastBookings.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        if (displayedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (selectedTab == 0) Icons.Default.EventBusy else Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (selectedTab == 0) "No active bookings right now" else "No completed bookings yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (selectedTab == 0) "Browse any of our 10 categories to book a pro!" else "Completed jobs and receipts will show up here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(displayedList) { booking ->
                    BookingItemCard(
                        booking = booking,
                        onAdvanceStatus = { onAdvanceStatus(booking) },
                        onCancelBooking = { onCancelBooking(booking.id) },
                        onReschedule = { onOpenReschedule(booking) },
                        onReview = { onOpenReview(booking) },
                        onCallPro = {
                            onCallPro(
                                booking.providerName,
                                "${booking.categoryTitle} Specialist",
                                booking.providerInitials,
                                booking.serviceTitle,
                                booking.contactPhone
                            )
                        },
                        onChatPro = {
                            onChatPro(
                                booking.providerName,
                                "${booking.categoryTitle} Specialist",
                                booking.providerInitials,
                                booking.serviceTitle,
                                booking.contactPhone
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BookingItemCard(
    booking: BookingEntity,
    onAdvanceStatus: () -> Unit,
    onCancelBooking: () -> Unit,
    onReschedule: () -> Unit,
    onReview: () -> Unit,
    onCallPro: () -> Unit,
    onChatPro: () -> Unit
) {
    val statusColor = when (booking.status) {
        "CONFIRMED" -> Color(0xFF2563EB)
        "EN_ROUTE" -> Color(0xFF0284C7)
        "IN_PROGRESS" -> Color(0xFFD97706)
        "COMPLETED" -> Color(0xFF059669)
        "CANCELLED" -> Color(0xFFDC2626)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("booking_card_${booking.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Category, Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.categoryTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = booking.status.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = booking.serviceTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Time & Location Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${booking.scheduledDate} • ${booking.scheduledTimeSlot}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = booking.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Stepper Timeline
            if (booking.status != "CANCELLED") {
                Spacer(modifier = Modifier.height(14.dp))
                StatusTimeline(currentStatus = booking.status)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Assigned Pro Profile & Contact Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = booking.providerInitials,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = booking.providerName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Total Paid: $${booking.totalPrice.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Action Buttons: Call & Chat
                if (booking.status != "CANCELLED") {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Message Chat Button
                        FilledTonalIconButton(
                            onClick = onChatPro,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Chat Pro",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Call Pro Button
                        FilledTonalIconButton(
                            onClick = onCallPro,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Call Pro",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Status Progress & Management Buttons
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (booking.status == "COMPLETED") {
                    if (booking.userRating > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Your Rating: ${booking.userRating}/5 ★ \"${booking.userReview}\"", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    } else {
                        Button(
                            onClick = onReview,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Rate & Review Specialist")
                        }
                    }
                } else if (booking.status != "CANCELLED") {
                    // Advance Status Demo button
                    OutlinedButton(
                        onClick = onAdvanceStatus,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = when (booking.status) {
                                "CONFIRMED" -> "Simulate: En Route"
                                "EN_ROUTE" -> "Simulate: Arrived"
                                "IN_PROGRESS" -> "Simulate: Complete"
                                else -> "Next Step"
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    // Reschedule
                    OutlinedButton(
                        onClick = onReschedule,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Reschedule", style = MaterialTheme.typography.labelMedium)
                    }

                    // Cancel
                    TextButton(
                        onClick = onCancelBooking,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusTimeline(currentStatus: String) {
    val steps = listOf("Confirmed", "En Route", "In Progress", "Completed")
    val currentIndex = when (currentStatus) {
        "CONFIRMED" -> 0
        "EN_ROUTE" -> 1
        "IN_PROGRESS" -> 2
        "COMPLETED" -> 3
        else -> 0
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isDone = index <= currentIndex
            val isCurrent = index == currentIndex

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCurrent) MaterialTheme.colorScheme.primary
                            else if (isDone) Color(0xFF10B981)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
