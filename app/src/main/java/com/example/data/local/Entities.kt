package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serviceId: String,
    val serviceTitle: String,
    val categoryTitle: String,
    val providerName: String,
    val providerInitials: String,
    val status: String, // "CONFIRMED", "EN_ROUTE", "IN_PROGRESS", "COMPLETED", "CANCELLED"
    val scheduledDate: String,
    val scheduledTimeSlot: String,
    val address: String,
    val contactPhone: String,
    val basePrice: Double,
    val addOnsDescription: String,
    val addOnsCost: Double,
    val isExpress: Boolean,
    val expressFee: Double,
    val totalPrice: Double,
    val specialInstructions: String,
    val userRating: Int = 0,
    val userReview: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val serviceId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_addresses")
data class SavedAddressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String,
    val street: String,
    val cityStateZip: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "downloaded_videos")
data class DownloadedVideo(
    @PrimaryKey val id: String,
    val title: String,
    val channel: String,
    val thumbnail_url: String,
    val local_path: String,
    val downloadedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_video_cross_ref",
    primaryKeys = ["playlistId", "videoId"],
    foreignKeys = [
        androidx.room.ForeignKey(entity = Playlist::class, parentColumns = ["id"], childColumns = ["playlistId"], onDelete = androidx.room.ForeignKey.CASCADE),
        androidx.room.ForeignKey(entity = DownloadedVideo::class, parentColumns = ["id"], childColumns = ["videoId"], onDelete = androidx.room.ForeignKey.CASCADE)
    ],
    indices = [androidx.room.Index("videoId")]
)
data class PlaylistVideoCrossRef(
    val playlistId: Long,
    val videoId: String
)
