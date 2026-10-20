package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BrageDao {

    // Bookings
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    fun getBookingById(id: Long): Flow<BookingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity): Long

    @Query("UPDATE bookings SET status = :status WHERE id = :id")
    suspend fun updateBookingStatus(id: Long, status: String)

    @Query("UPDATE bookings SET userRating = :rating, userReview = :review WHERE id = :id")
    suspend fun updateBookingReview(id: Long, rating: Int, review: String)

    @Query("UPDATE bookings SET scheduledDate = :newDate, scheduledTimeSlot = :newTime WHERE id = :id")
    suspend fun rescheduleBooking(id: Long, newDate: String, newTime: String)

    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun deleteBooking(id: Long)

    // Favorites
    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE serviceId = :serviceId")
    suspend fun deleteFavorite(serviceId: String)

    @Query("SELECT COUNT(*) FROM favorites WHERE serviceId = :serviceId")
    fun isFavorite(serviceId: String): Flow<Int>

    // Addresses
    @Query("SELECT * FROM saved_addresses ORDER BY isDefault DESC, id ASC")
    fun getAllAddresses(): Flow<List<SavedAddressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: SavedAddressEntity): Long

    @Query("DELETE FROM saved_addresses WHERE id = :id")
    suspend fun deleteAddress(id: Long)

    // Downloaded Videos
    @Query("SELECT * FROM downloaded_videos ORDER BY downloadedAt DESC")
    fun getAllDownloadedVideos(): Flow<List<DownloadedVideo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadedVideo(video: DownloadedVideo)

    @Query("DELETE FROM downloaded_videos WHERE id = :videoId")
    suspend fun deleteDownloadedVideo(videoId: String)

    // Playlists
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistVideoCrossRef(crossRef: PlaylistVideoCrossRef)

    @Query("DELETE FROM playlist_video_cross_ref WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun deletePlaylistVideo(playlistId: Long, videoId: String)

    @Query("""
        SELECT downloaded_videos.* FROM downloaded_videos
        INNER JOIN playlist_video_cross_ref ON downloaded_videos.id = playlist_video_cross_ref.videoId
        WHERE playlist_video_cross_ref.playlistId = :playlistId
    """)
    fun getVideosInPlaylist(playlistId: Long): Flow<List<DownloadedVideo>>
}
