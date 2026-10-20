package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class ServiceCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconName: String,
    val accentColorHex: Long,
    val popularTag: String? = null,
    val availableCount: Int
)

data class ServiceAddOn(
    val id: String,
    val title: String,
    val price: Double,
    val durationMin: Int = 15
)

data class ProviderInfo(
    val id: String,
    val name: String,
    val title: String,
    val rating: Double,
    val reviewCount: Int,
    val completedJobs: Int,
    val experienceYears: Int,
    val badge: String,
    val initials: String,
    val bio: String,
    val isVerified: Boolean = true,
    val phoneNumber: String = "+1 (541) 555-0143",
    val latitude: Double = 44.0462,
    val longitude: Double = -123.0220,
    val locationName: String = "Springfield, OR",
    val serviceRadiusMiles: Int = 20,
    val distanceMiles: Double = 1.6,
    val responseTimeText: String = "Arrives in 25-40 min",
    val hourlyRate: Double = 65.0
)

data class PortfolioItem(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val completionDate: String,
    val durationHours: Double,
    val rating: Double = 5.0,
    val beforeState: String = "Before: Heavy wear and unresolved issue",
    val afterState: String = "After: Completely restored and sealed to perfection",
    val tags: List<String> = listOf("Completed On-Site", "5★ Verified Result"),
    val costEstimate: String = "$120 - $180"
)

data class ServiceLocationInfo(
    val cityState: String = "Springfield, OR",
    val address: String = "Main St, Springfield, OR 97477",
    val latitude: Double = 44.0462,
    val longitude: Double = -123.0220,
    val label: String = "Primary Service Address"
)

data class ServiceItem(
    val id: String,
    val categoryId: String,
    val title: String,
    val subtitle: String,
    val basePrice: Double,
    val priceUnit: String, // "/ job", "/ hr", "/ room"
    val rating: Double,
    val reviewCount: Int,
    val durationMinutes: Int,
    val description: String,
    val includedHighlights: List<String>,
    val provider: ProviderInfo,
    val instantBookable: Boolean = true,
    val expressAvailable: Boolean = true,
    val addOns: List<ServiceAddOn> = emptyList()
)

data class ReviewItem(
    val id: String,
    val userName: String,
    val rating: Int,
    val date: String,
    val comment: String
)

data class CustomerTestimonial(
    val id: String,
    val providerId: String,
    val authorName: String,
    val rating: Int,
    val dateText: String,
    val serviceBooked: String,
    val comment: String,
    val isVerified: Boolean = true,
    val helpfulCount: Int = 0,
    val tags: List<String> = emptyList(),
    val wouldRecommend: Boolean = true,
    val bookingId: Long? = null
)

data class SavedCardInfo(
    val id: String,
    val cardholderName: String,
    val cardNumberMasked: String,
    val expiry: String,
    val cardType: String,
    val nickname: String,
    val isDefault: Boolean = false,
    val isBrageProCard: Boolean = false
)

data class ProviderApplicationInfo(
    val id: String,
    val businessName: String,
    val contactName: String,
    val phone: String,
    val email: String,
    val categoryId: String,
    val categoryTitle: String,
    val serviceAreaRadiusMiles: Int,
    val hourlyRate: Double,
    val licenseNumber: String,
    val insurancePolicyNumber: String,
    val experienceYears: Int,
    val description: String,
    val submittedAt: String,
    val status: String = "UNDER_REVIEW",
    val signLater: Boolean = false,
    val digitalSignature: String = ""
)

data class ChatThread(
    val id: String,
    val contactName: String,
    val contactRole: String,
    val contactInitials: String,
    val contextTitle: String,
    val phoneNumber: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = true,
    val isSupport: Boolean = false,
    val bookingId: Long? = null,
    val category: String = "All"
)

data class UserProfile(
    val fullName: String = "Alex Morgan",
    val username: String = "alex_morgan",
    val email: String = "alex.morgan@example.com",
    val phone: String = "+1 (541) 555-0199",
    val profilePhotoUri: String? = null,
    val avatarPresetId: String = "preset_default",
    val avatarBgColorHex: Long = 0xFF2563EB,
    val memberSince: String = "October 2024",
    val bio: String = "Homeowner in Springfield, OR. Verified Brage Member.",
    val usernameLastUpdatedMillis: Long = 0L
)

