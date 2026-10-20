package com.example.data.repository

import com.example.data.local.DownloadedVideo
import com.example.data.local.Playlist
import com.example.data.local.PlaylistVideoCrossRef
import kotlinx.coroutines.flow.Flow

interface VideoRepository {
    fun getAllDownloadedVideos(): Flow<List<DownloadedVideo>>
    suspend fun insertDownloadedVideo(video: DownloadedVideo)
    suspend fun deleteDownloadedVideo(videoId: String)

    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(name: String): Long
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addVideoToPlaylist(playlistId: Long, videoId: String)
    suspend fun removeVideoFromPlaylist(playlistId: Long, videoId: String)
    fun getVideosInPlaylist(playlistId: Long): Flow<List<DownloadedVideo>>
}
