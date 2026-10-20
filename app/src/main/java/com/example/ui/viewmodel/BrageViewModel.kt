package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BookingEntity
import com.example.data.local.DownloadedVideo
import com.example.data.local.Playlist
import com.example.data.local.SavedAddressEntity
import com.example.data.model.ServiceCategory
import com.example.data.model.ServiceItem
import com.example.data.repository.BrageRepository
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

sealed class ScreenDestination {
    object Home : ScreenDestination()
    object ChatHub : ScreenDestination()
    object AllCategories : ScreenDestination()
    data class CategoryDetail(val categoryId: String) : ScreenDestination()
    data class ServiceDetail(val serviceId: String) : ScreenDestination()
    data class BookingDetail(val bookingId: Long) : ScreenDestination()
    data class CallScreen(
        val contactName: String,
        val contactRole: String,
        val contactInitials: String,
        val contextTitle: String,
        val phoneNumber: String
    ) : ScreenDestination()
    data class VideoCallScreen(
        val contactName: String,
        val contactRole: String,
        val contactInitials: String,
        val contextTitle: String,
        val phoneNumber: String
    ) : ScreenDestination()
    data class ChatScreen(
        val contactName: String,
        val contactRole: String,
        val contactInitials: String,
        val contextTitle: String,
        val phoneNumber: String
    ) : ScreenDestination()
    object BookingsList : ScreenDestination()
    object Contract : ScreenDestination()
    object Application : ScreenDestination()
    object DeilaAi : ScreenDestination()
    object DeilaPlay : ScreenDestination()
    data class DeilaPlayScreen(
        val videoId: String = "synth_neon",
        val videoTitle: String = "How I Built This Neon Synth in 24 Hours | DIY Electronics",
        val channelInfo: String = "1.2M subs • 87K views • 2 days ago"
    ) : ScreenDestination()
    data class ShortPage(val initialShortId: String = "s1") : ScreenDestination()
    data class DownloadedVideoPlay(val video: DownloadedVideo) : ScreenDestination()
    data class VideoPlay(
        val videoId: String = "media_1",
        val videoTitle: String = "Cyber Electric Diagnosis & Motor Windings 101",
        val videoCategory: String = "Tech Diagnosis",
        val videoDuration: String = "14:20",
        val creatorName: String = "Master Tech Marcus Chen",
        val initialTimestampSeconds: Int = 0
    ) : ScreenDestination()
    data class ProviderProfile(val providerId: String) : ScreenDestination()
    object SavedServices : ScreenDestination()
    object ProProfile : ScreenDestination()
    object ProfileAndSupport : ScreenDestination()
    object CardApplication : ScreenDestination()
    object ApplyProvider : ScreenDestination()
    data class MapLocation(val providerId: String? = null) : ScreenDestination()
    object EditProfilePicture : ScreenDestination()
    data class ViewProfilePicture(
        val isDeilaAiView: Boolean = false,
        val customTitle: String? = null
    ) : ScreenDestination()
    object EditUsername : ScreenDestination()
    object AccountVerification : ScreenDestination()
    object Settings : ScreenDestination()
}

enum class NavigationTab {
    HOME,
    CHAT,
    DEILA_PLAY,
    DEILA_AI,
    BOOKINGS,
    PROFILE
}

enum class ServiceFilterOption(val label: String) {
    ALL("All Services"),
    TOP_RATED("Top Rated (4.9+)"),
    EXPRESS("Express Dispatch"),
    INSTANT("Instant Book")
}

data class UiState(
    val currentTab: NavigationTab = NavigationTab.HOME,
    val currentScreen: ScreenDestination = ScreenDestination.Home,
    val navigationStack: List<ScreenDestination> = listOf(ScreenDestination.Home),
    val searchQuery: String = "",
    val activeFilter: ServiceFilterOption = ServiceFilterOption.ALL,
    val selectedCategoryId: String? = null,
    val bookingSuccessModal: BookingEntity? = null,
    val reviewTargetBooking: BookingEntity? = null,
    val rescheduleTargetBooking: BookingEntity? = null,
    val statusAdvanceToast: String? = null,
    val downloadProgress: Map<String, Float> = emptyMap()
)

class BrageViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val repository = BrageRepository(database.brageDao())
    val videoRepository: VideoRepository = BrageRepository(database.brageDao())

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val categories: List<ServiceCategory> = repository.categories
    val allServices: List<ServiceItem> = repository.allServices

    val bookings: StateFlow<List<BookingEntity>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<String>> = repository.allFavorites
        .map { list -> list.map { it.serviceId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedVideos: StateFlow<List<DownloadedVideo>> = videoRepository.getAllDownloadedVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = videoRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            videoRepository.createPlaylist(name)
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            videoRepository.deletePlaylist(id)
        }
    }

    fun addVideoToPlaylist(playlistId: Long, videoId: String) {
        viewModelScope.launch {
            videoRepository.addVideoToPlaylist(playlistId, videoId)
        }
    }

    fun removeVideoFromPlaylist(playlistId: Long, videoId: String) {
        viewModelScope.launch {
            videoRepository.removeVideoFromPlaylist(playlistId, videoId)
        }
    }

    val addresses: StateFlow<List<SavedAddressEntity>> = repository.allAddresses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun triggerRefresh(customMsg: String? = null) {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(1500L)
            _isRefreshing.value = false
            _uiState.update {
                it.copy(statusAdvanceToast = customMsg ?: "DEILA PLAY: Content and live streams refreshed!")
            }
        }
    }

    fun triggerDownload(videoId: String, title: String, channel: String, thumbUrl: String) {
        viewModelScope.launch {
            // Add to progress map
            _uiState.update { it.copy(downloadProgress = it.downloadProgress + (videoId to 0f)) }

            // Simulate progress
            for (i in 1..5) {
                delay(500L)
                val progress = i * 0.2f
                _uiState.update { it.copy(downloadProgress = it.downloadProgress + (videoId to progress)) }
            }

            // Save to DB
            videoRepository.insertDownloadedVideo(
                DownloadedVideo(
                    id = videoId,
                    title = title,
                    channel = channel,
                    thumbnail_url = thumbUrl,
                    local_path = "/data/user/0/com.aistudio.brage.xkqwrt/files/videos/$videoId.mp4"
                )
            )

            // Remove from progress map
            _uiState.update { it.copy(downloadProgress = it.downloadProgress - videoId) }
            _uiState.update { it.copy(statusAdvanceToast = "'$title' downloaded successfully!") }
        }
    }

    private val _extraPagedServices = MutableStateFlow<List<ServiceItem>>(emptyList())
    val extraPagedServices: StateFlow<List<ServiceItem>> = _extraPagedServices.asStateFlow()

    fun loadMoreServices() {
        if (_isLoadingMore.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            delay(1000L)
            val baseList = repository.allServices
            val batchNumber = (_extraPagedServices.value.size / baseList.size) + 1
            val newBatch = baseList.mapIndexed { index, item ->
                item.copy(
                    id = "${item.id}_page_$batchNumber",
                    title = "${item.title} (Page $batchNumber)"
                )
            }
            _extraPagedServices.value = _extraPagedServices.value + newBatch
            _isLoadingMore.value = false
        }
    }

    // Filtered services based on search query, category, and filter options
    val displayedServices: StateFlow<List<ServiceItem>> = combine(
        _uiState,
        favorites,
        _extraPagedServices
    ) { state, favs, extraList ->
        var list = repository.allServices + extraList

        // Filter by category if selected
        state.selectedCategoryId?.let { catId ->
            list = list.filter { it.categoryId == catId }
        }

        // Filter by search query
        if (state.searchQuery.isNotBlank()) {
            val q = state.searchQuery.trim().lowercase()
            list = list.filter { item ->
                item.title.lowercase().contains(q) ||
                item.subtitle.lowercase().contains(q) ||
                item.description.lowercase().contains(q) ||
                item.provider.name.lowercase().contains(q) ||
                item.provider.badge.lowercase().contains(q)
            }
        }

        // Filter by option pills
        list = when (state.activeFilter) {
            ServiceFilterOption.ALL -> list
            ServiceFilterOption.TOP_RATED -> list.filter { it.rating >= 4.93 }
            ServiceFilterOption.EXPRESS -> list.filter { it.expressAvailable }
            ServiceFilterOption.INSTANT -> list.filter { it.instantBookable }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), allServices)

    // Navigation actions
    fun selectTab(tab: NavigationTab) {
        val destination = when (tab) {
            NavigationTab.HOME -> ScreenDestination.Home
            NavigationTab.CHAT -> ScreenDestination.ChatHub
            NavigationTab.DEILA_PLAY -> ScreenDestination.DeilaPlay
            NavigationTab.DEILA_AI -> ScreenDestination.DeilaAi
            NavigationTab.BOOKINGS -> ScreenDestination.BookingsList
            NavigationTab.PROFILE -> ScreenDestination.ProfileAndSupport
        }
        _uiState.update {
            it.copy(
                currentTab = tab,
                currentScreen = destination,
                navigationStack = listOf(destination),
                searchQuery = "",
                selectedCategoryId = null
            )
        }
    }

    fun navigateTo(destination: ScreenDestination) {
        _uiState.update { current ->
            current.copy(
                currentScreen = destination,
                navigationStack = current.navigationStack + destination
            )
        }
    }

    fun navigateBack(): Boolean {
        val currentStack = _uiState.value.navigationStack
        if (currentStack.size > 1) {
            val newStack = currentStack.dropLast(1)
            val prevDestination = newStack.last()
            val newTab = when (prevDestination) {
                is ScreenDestination.Home -> NavigationTab.HOME
                is ScreenDestination.ChatHub -> NavigationTab.CHAT
                is ScreenDestination.DeilaPlay -> NavigationTab.DEILA_PLAY
                is ScreenDestination.ShortPage -> NavigationTab.DEILA_PLAY
                is ScreenDestination.VideoPlay -> NavigationTab.DEILA_PLAY
                is ScreenDestination.DeilaAi -> NavigationTab.DEILA_AI
                is ScreenDestination.BookingsList -> NavigationTab.BOOKINGS
                is ScreenDestination.ProfileAndSupport -> NavigationTab.PROFILE
                else -> _uiState.value.currentTab
            }
            _uiState.update {
                it.copy(
                    currentScreen = prevDestination,
                    navigationStack = newStack,
                    currentTab = newTab
                )
            }
            return true
        } else if (_uiState.value.currentTab == NavigationTab.DEILA_PLAY ||
            _uiState.value.currentScreen is ScreenDestination.DeilaPlay ||
            _uiState.value.currentScreen is ScreenDestination.ShortPage ||
            _uiState.value.currentScreen is ScreenDestination.VideoPlay) {
            selectTab(NavigationTab.HOME)
            return true
        }
        return false
    }

    fun setSearchQuery(query: String) {
        _extraPagedServices.value = emptyList()
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setFilter(filter: ServiceFilterOption) {
        _extraPagedServices.value = emptyList()
        _uiState.update { it.copy(activeFilter = filter) }
    }

    fun selectCategory(categoryId: String?) {
        _extraPagedServices.value = emptyList()
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        if (categoryId != null) {
            navigateTo(ScreenDestination.CategoryDetail(categoryId))
        }
    }

    // Toggle favorite in Room
    fun toggleFavorite(serviceId: String) {
        viewModelScope.launch {
            val isFav = favorites.value.contains(serviceId)
            repository.toggleFavorite(serviceId, isFav)
        }
    }

    // Book service action
    fun createBooking(
        service: ServiceItem,
        scheduledDate: String,
        timeSlot: String,
        address: String,
        phone: String,
        selectedAddOns: List<com.example.data.model.ServiceAddOn>,
        isExpress: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            val addOnsCost = selectedAddOns.sumOf { it.price }
            val expressFee = if (isExpress) 15.0 else 0.0
            val total = service.basePrice + addOnsCost + expressFee
            val addOnsDescription = if (selectedAddOns.isEmpty()) {
                "Standard Service"
            } else {
                selectedAddOns.joinToString(", ") { it.title }
            }

            val category = repository.getCategoryById(service.categoryId)

            val newBooking = BookingEntity(
                serviceId = service.id,
                serviceTitle = service.title,
                categoryTitle = category?.title ?: "On-Demand Service",
                providerName = service.provider.name,
                providerInitials = service.provider.initials,
                status = "CONFIRMED",
                scheduledDate = scheduledDate,
                scheduledTimeSlot = timeSlot,
                address = address,
                contactPhone = phone,
                basePrice = service.basePrice,
                addOnsDescription = addOnsDescription,
                addOnsCost = addOnsCost,
                isExpress = isExpress,
                expressFee = expressFee,
                totalPrice = total,
                specialInstructions = notes
            )

            val id = repository.createBooking(newBooking)
            val insertedBooking = newBooking.copy(id = id)
            _uiState.update { it.copy(bookingSuccessModal = insertedBooking) }
        }
    }

    fun dismissSuccessModal() {
        _uiState.update { it.copy(bookingSuccessModal = null) }
    }

    // Advance booking status (for real user interactivity simulation)
    fun advanceBookingStatus(booking: BookingEntity) {
        viewModelScope.launch {
            val nextStatus = when (booking.status) {
                "CONFIRMED" -> "EN_ROUTE"
                "EN_ROUTE" -> "IN_PROGRESS"
                "IN_PROGRESS" -> "COMPLETED"
                else -> booking.status
            }
            if (nextStatus != booking.status) {
                repository.updateBookingStatus(booking.id, nextStatus)
                val msg = "Booking status updated: $nextStatus"
                _uiState.update { it.copy(statusAdvanceToast = msg) }
            }
        }
    }

    fun cancelBooking(bookingId: Long) {
        viewModelScope.launch {
            repository.cancelBooking(bookingId)
            _uiState.update { it.copy(statusAdvanceToast = "Booking has been cancelled") }
        }
    }

    fun clearStatusToast() {
        _uiState.update { it.copy(statusAdvanceToast = null) }
    }

    // Review dialogs
    fun openReviewModal(booking: BookingEntity) {
        _uiState.update { it.copy(reviewTargetBooking = booking) }
    }

    fun dismissReviewModal() {
        _uiState.update { it.copy(reviewTargetBooking = null) }
    }

    fun submitReview(bookingId: Long, rating: Int, reviewText: String) {
        viewModelScope.launch {
            repository.submitReview(bookingId, rating, reviewText)
            val booking = bookings.value.find { it.id == bookingId }
            if (booking != null) {
                val service = allServices.find { it.id == booking.serviceId }
                val providerId = service?.provider?.id ?: "p_elena"
                submitProviderReview(
                    providerId = providerId,
                    authorName = "Alex Morgan",
                    rating = rating,
                    comment = reviewText,
                    serviceBooked = booking.serviceTitle,
                    tags = listOf("⚡ Punctual", "💯 Quality Guaranteed"),
                    wouldRecommend = rating >= 4,
                    bookingId = bookingId
                )
            }
            _uiState.update {
                it.copy(
                    reviewTargetBooking = null,
                    statusAdvanceToast = "Thank you! Rating & review submitted."
                )
            }
        }
    }

    // Reschedule dialog
    fun openRescheduleModal(booking: BookingEntity) {
        _uiState.update { it.copy(rescheduleTargetBooking = booking) }
    }

    fun dismissRescheduleModal() {
        _uiState.update { it.copy(rescheduleTargetBooking = null) }
    }

    fun submitReschedule(bookingId: Long, newDate: String, newTime: String) {
        viewModelScope.launch {
            repository.reschedule(bookingId, newDate, newTime)
            _uiState.update {
                it.copy(
                    rescheduleTargetBooking = null,
                    statusAdvanceToast = "Service rescheduled to $newDate ($newTime)"
                )
            }
        }
    }

    // Start Call Page
    fun startCall(
        name: String,
        role: String,
        initials: String,
        contextTitle: String,
        phone: String
    ) {
        navigateTo(
            ScreenDestination.CallScreen(
                contactName = name,
                contactRole = role,
                contactInitials = initials,
                contextTitle = contextTitle,
                phoneNumber = phone
            )
        )
    }

    // Start Video Call Page
    fun startVideoCall(
        name: String,
        role: String,
        initials: String,
        contextTitle: String,
        phone: String
    ) {
        navigateTo(
            ScreenDestination.VideoCallScreen(
                contactName = name,
                contactRole = role,
                contactInitials = initials,
                contextTitle = contextTitle,
                phoneNumber = phone
            )
        )
    }

    // Open Video Play Screen
    fun openVideoPlay(
        videoId: String = "media_1",
        videoTitle: String = "Cyber Electric Diagnosis & Motor Windings 101",
        videoCategory: String = "Tech Diagnosis",
        videoDuration: String = "14:20",
        creatorName: String = "Master Tech Marcus Chen",
        initialTimestampSeconds: Int = 0
    ) {
        navigateTo(
            ScreenDestination.VideoPlay(
                videoId = videoId,
                videoTitle = videoTitle,
                videoCategory = videoCategory,
                videoDuration = videoDuration,
                creatorName = creatorName,
                initialTimestampSeconds = initialTimestampSeconds
            )
        )
    }

    // Open Dedicated Short Page
    fun openShortPage(shortId: String = "s1") {
        navigateTo(ScreenDestination.ShortPage(initialShortId = shortId))
    }

    // Open Downloaded Video
    fun openDownloadedVideo(video: DownloadedVideo) {
        navigateTo(ScreenDestination.DownloadedVideoPlay(video = video))
    }

    // Chat Threads State
    val chatThreads = MutableStateFlow<List<com.example.data.model.ChatThread>>(
        listOf(
            com.example.data.model.ChatThread(
                id = "thread_dispatch_alert",
                contactName = "⚡ Dispatch Notification",
                contactRole = "Automated Live Dispatch Alert",
                contactInitials = "⚡",
                contextTitle = "Live Pro En-Route Telemetry",
                phoneNumber = "(541) 555-0188",
                lastMessage = "Marcus Vance is en route with service vehicle (ETA 12-15 min). Tap to message pro.",
                timestamp = "Just now",
                unreadCount = 1,
                isOnline = true,
                isSupport = false,
                bookingId = 9024L,
                category = "Notifications"
            ),
            com.example.data.model.ChatThread(
                id = "thread_support",
                contactName = "Brage Live Concierge",
                contactRole = "24/7 Priority Support",
                contactInitials = "BC",
                contextTitle = "Live Concierge & Dispatch",
                phoneNumber = "1-800-555-BRAGE",
                lastMessage = "Hello! Your $1,000,000 Brage Service Guarantee is active for all verified bookings.",
                timestamp = "3m ago",
                unreadCount = 1,
                isOnline = true,
                isSupport = true,
                category = "Support"
            ),
            com.example.data.model.ChatThread(
                id = "thread_booking_notice",
                contactName = "🔔 Booking Notification",
                contactRole = "System Status Update",
                contactInitials = "🔔",
                contextTitle = "Whole-House Deep Cleaning Confirmed",
                phoneNumber = "(541) 555-0143",
                lastMessage = "Appointment #BK-9021 confirmed with Elena Rostova. Tools & sanitization gear prepared.",
                timestamp = "8m ago",
                unreadCount = 1,
                isOnline = true,
                isSupport = false,
                bookingId = 9021L,
                category = "Notifications"
            ),
            com.example.data.model.ChatThread(
                id = "thread_elena",
                contactName = "Elena Rostova",
                contactRole = "Deep Clean Specialist",
                contactInitials = "ER",
                contextTitle = "Whole-House Deep Cleaning",
                phoneNumber = "(541) 555-0143",
                lastMessage = "I am preparing the hospital-grade sanitization kit and on schedule for your appointment.",
                timestamp = "12m ago",
                unreadCount = 0,
                isOnline = true,
                isSupport = false,
                bookingId = 9021L,
                category = "Bookings"
            ),
            com.example.data.model.ChatThread(
                id = "thread_marcus",
                contactName = "Marcus Vance",
                contactRole = "Master Electrician",
                contactInitials = "MV",
                contextTitle = "EV Charger 240V Installation",
                phoneNumber = "(541) 555-0188",
                lastMessage = "I have the 50A breaker switch and mounting bracket in the service truck.",
                timestamp = "1h ago",
                unreadCount = 0,
                isOnline = true,
                isSupport = false,
                bookingId = 9024L,
                category = "Bookings"
            ),
            com.example.data.model.ChatThread(
                id = "thread_david",
                contactName = "David Chen",
                contactRole = "Senior HVAC Technician",
                contactInitials = "DC",
                contextTitle = "Heat Pump & AC Maintenance",
                phoneNumber = "(541) 555-0199",
                lastMessage = "Diagnostic tools calibrated. I will call you 15 minutes before reaching your address.",
                timestamp = "Yesterday",
                unreadCount = 0,
                isOnline = false,
                isSupport = false,
                category = "Pros"
            ),
            com.example.data.model.ChatThread(
                id = "thread_sarah",
                contactName = "Sarah Jenkins",
                contactRole = "Landscape Architect",
                contactInitials = "SJ",
                contextTitle = "Garden Drought-Resistant Design",
                phoneNumber = "(541) 555-0177",
                lastMessage = "Shared the 3D plant layout mockups for your front garden review!",
                timestamp = "2d ago",
                unreadCount = 0,
                isOnline = true,
                isSupport = false,
                category = "Pros"
            )
        )
    )

    // Start Chat Page
    fun startChat(
        name: String,
        role: String,
        initials: String,
        contextTitle: String,
        phone: String
    ) {
        // Mark or update thread if existing
        chatThreads.update { list ->
            val existing = list.find { it.contactName.equals(name, ignoreCase = true) }
            if (existing != null) {
                list.map {
                    if (it.id == existing.id) it.copy(unreadCount = 0) else it
                }
            } else {
                val newThread = com.example.data.model.ChatThread(
                    id = "thread_${System.currentTimeMillis()}",
                    contactName = name,
                    contactRole = role,
                    contactInitials = initials,
                    contextTitle = contextTitle,
                    phoneNumber = phone,
                    lastMessage = "Conversation started",
                    timestamp = "Just now",
                    unreadCount = 0,
                    isOnline = true,
                    isSupport = name.contains("Concierge", ignoreCase = true) || name.contains("Support", ignoreCase = true),
                    category = if (name.contains("Concierge", ignoreCase = true)) "Support" else "Pros"
                )
                listOf(newThread) + list
            }
        }

        navigateTo(
            ScreenDestination.ChatScreen(
                contactName = name,
                contactRole = role,
                contactInitials = initials,
                contextTitle = contextTitle,
                phoneNumber = phone
            )
        )
    }

    // Open Provider Profile
    fun openProviderProfile(providerId: String) {
        navigateTo(ScreenDestination.ProviderProfile(providerId))
    }

    // Payment Cards & Provider Applications State
    val savedCards = MutableStateFlow<List<com.example.data.model.SavedCardInfo>>(
        listOf(
            com.example.data.model.SavedCardInfo(
                id = "card_1",
                cardholderName = "ALEX MORGAN",
                cardNumberMasked = "•••• •••• •••• 4242",
                expiry = "09/28",
                cardType = "Visa",
                nickname = "Primary Visa Card",
                isDefault = true,
                isBrageProCard = false
            ),
            com.example.data.model.SavedCardInfo(
                id = "card_2",
                cardholderName = "ALEX MORGAN",
                cardNumberMasked = "•••• •••• •••• 8821",
                expiry = "11/29",
                cardType = "Brage Pro Card",
                nickname = "Brage 5% Cashback Rewards",
                isDefault = false,
                isBrageProCard = true
            )
        )
    )

    val providerApplications = MutableStateFlow<List<com.example.data.model.ProviderApplicationInfo>>(emptyList())

    val currentServiceLocation = MutableStateFlow(com.example.data.model.ServiceLocationInfo())

    fun openMapLocation(providerId: String? = null) {
        navigateTo(ScreenDestination.MapLocation(providerId))
    }

    fun updateServiceLocation(loc: com.example.data.model.ServiceLocationInfo) {
        currentServiceLocation.value = loc
        _uiState.update {
            it.copy(statusAdvanceToast = "Service location updated to ${loc.cityState}")
        }
    }

    fun openCardApplication() {
        navigateTo(ScreenDestination.CardApplication)
    }

    fun openApplyProvider() {
        navigateTo(ScreenDestination.ApplyProvider)
    }

    fun addSavedCard(card: com.example.data.model.SavedCardInfo) {
        val current = savedCards.value.toMutableList()
        if (card.isDefault) {
            val updated = current.map { it.copy(isDefault = false) }.toMutableList()
            updated.add(0, card)
            savedCards.value = updated
        } else {
            current.add(card)
            savedCards.value = current
        }
    }

    fun removeSavedCard(cardId: String) {
        savedCards.value = savedCards.value.filterNot { it.id == cardId }
    }

    fun submitProviderApplication(app: com.example.data.model.ProviderApplicationInfo) {
        val current = providerApplications.value.toMutableList()
        current.add(0, app)
        providerApplications.value = current
    }

    fun completeApplicationSignature(appId: String, signature: String) {
        val updated = providerApplications.value.map { app ->
            if (app.id == appId) {
                app.copy(signLater = false, digitalSignature = signature, status = "UNDER_REVIEW")
            } else app
        }
        providerApplications.value = updated
        _uiState.update { it.copy(statusAdvanceToast = "Agreement signed successfully! Application updated.") }
    }

    // Provider Reviews State
    val providerReviews = MutableStateFlow<Map<String, List<com.example.data.model.CustomerTestimonial>>>(emptyMap())

    fun getReviewsForProvider(providerId: String): List<com.example.data.model.CustomerTestimonial> {
        val existing = providerReviews.value[providerId]
        if (existing != null) return existing

        val provider = allServices.find { it.provider.id == providerId }?.provider
        val pName = provider?.name ?: "Specialist"
        val serviceName = allServices.find { it.provider.id == providerId }?.title ?: "On-Demand Service"

        val defaultList = listOf(
            com.example.data.model.CustomerTestimonial(
                id = "${providerId}_t1",
                providerId = providerId,
                authorName = "Marcus Aurelius",
                rating = 5,
                dateText = "3 days ago",
                serviceBooked = serviceName,
                comment = "Absolutely exceptional service from $pName. Arrived exactly on the minute, brought professional gear, and did a flawless job without cutting corners. Very respectful of our home!",
                isVerified = true,
                helpfulCount = 8,
                tags = listOf("⚡ Punctual & On-Time", "🧼 Left Area Spotless", "💯 Quality Guaranteed"),
                wouldRecommend = true
            ),
            com.example.data.model.CustomerTestimonial(
                id = "${providerId}_t2",
                providerId = providerId,
                authorName = "Samantha Miller",
                rating = 5,
                dateText = "1 week ago",
                serviceBooked = serviceName,
                comment = "Professional, polite, and transparent about rates upfront. Zero surprise charges and clean work area left behind. Will definitely hire again!",
                isVerified = true,
                helpfulCount = 4,
                tags = listOf("💬 Clear Communication", "💰 Fair Upfront Pricing"),
                wouldRecommend = true
            ),
            com.example.data.model.CustomerTestimonial(
                id = "${providerId}_t3",
                providerId = providerId,
                authorName = "David K.",
                rating = 4,
                dateText = "2 weeks ago",
                serviceBooked = serviceName,
                comment = "Very knowledgeable and resolved the problem thoroughly. Great attention to detail and good communication throughout the job.",
                isVerified = true,
                helpfulCount = 2,
                tags = listOf("🔧 Expert Craftsmanship", "⏱️ Fast & Efficient"),
                wouldRecommend = true
            )
        )
        val updatedMap = providerReviews.value.toMutableMap()
        updatedMap[providerId] = defaultList
        providerReviews.value = updatedMap
        return defaultList
    }

    fun submitProviderReview(
        providerId: String,
        authorName: String,
        rating: Int,
        comment: String,
        serviceBooked: String,
        tags: List<String> = emptyList(),
        wouldRecommend: Boolean = true,
        bookingId: Long? = null
    ) {
        val currentList = getReviewsForProvider(providerId).toMutableList()
        val newReview = com.example.data.model.CustomerTestimonial(
            id = "rev_${System.currentTimeMillis()}",
            providerId = providerId,
            authorName = authorName.ifBlank { "Alex Morgan (Verified Customer)" },
            rating = rating.coerceIn(1, 5),
            dateText = "Just now",
            serviceBooked = serviceBooked.ifBlank { "Completed Service" },
            comment = comment.ifBlank { "Excellent service delivered with great professionalism and attention to detail." },
            isVerified = true,
            helpfulCount = 0,
            tags = tags,
            wouldRecommend = wouldRecommend,
            bookingId = bookingId
        )
        currentList.add(0, newReview)
        val map = providerReviews.value.toMutableMap()
        map[providerId] = currentList
        providerReviews.value = map

        if (bookingId != null) {
            viewModelScope.launch {
                repository.submitReview(bookingId, rating, comment)
            }
        } else {
            val unreviewed = bookings.value.find { b ->
                b.status == "COMPLETED" && b.userRating == 0 &&
                (allServices.find { it.id == b.serviceId }?.provider?.id == providerId ||
                 b.providerName.equals(allServices.find { it.provider.id == providerId }?.provider?.name, ignoreCase = true))
            }
            if (unreviewed != null) {
                viewModelScope.launch {
                    repository.submitReview(unreviewed.id, rating, comment)
                }
            }
        }

        _uiState.update {
            it.copy(
                statusAdvanceToast = "Thank you! Your $rating★ review has been published."
            )
        }
    }

    fun toggleHelpfulReview(providerId: String, reviewId: String) {
        val list = getReviewsForProvider(providerId).map {
            if (it.id == reviewId) it.copy(helpfulCount = it.helpfulCount + 1) else it
        }
        val map = providerReviews.value.toMutableMap()
        map[providerId] = list
        providerReviews.value = map
    }

    // Add address
    fun addAddress(label: String, street: String, cityStateZip: String) {
        viewModelScope.launch {
            repository.addAddress(
                SavedAddressEntity(
                    label = label,
                    street = street,
                    cityStateZip = cityStateZip,
                    isDefault = false
                )
            )
        }
    }

    // User Profile & Settings
    val userProfile = MutableStateFlow(com.example.data.model.UserProfile())
    val darkModeEnabled = MutableStateFlow(false)
    val dynamicColorsEnabled = MutableStateFlow(true)

    fun setDarkMode(enabled: Boolean) {
        darkModeEnabled.value = enabled
    }

    fun setDynamicColors(enabled: Boolean) {
        dynamicColorsEnabled.value = enabled
    }

    fun openEditProfilePicture() {
        navigateTo(ScreenDestination.EditProfilePicture)
    }

    fun openViewProfilePicture(isDeilaAiView: Boolean = false, customTitle: String? = null) {
        navigateTo(ScreenDestination.ViewProfilePicture(isDeilaAiView = isDeilaAiView, customTitle = customTitle))
    }

    fun openEditUsername() {
        navigateTo(ScreenDestination.EditUsername)
    }

    fun updateProfilePicture(photoUri: String?, presetId: String, bgColorHex: Long) {
        userProfile.update {
            it.copy(
                profilePhotoUri = photoUri,
                avatarPresetId = presetId,
                avatarBgColorHex = bgColorHex
            )
        }
        _uiState.update {
            it.copy(statusAdvanceToast = "Profile picture updated successfully!")
        }
        navigateBack()
    }

    fun updateProfileInfo(fullName: String, username: String, bio: String) {
        userProfile.update {
            it.copy(
                fullName = fullName,
                username = username,
                bio = bio
            )
        }
        _uiState.update {
            it.copy(statusAdvanceToast = "Account details saved for @$username")
        }
        navigateBack()
    }

    fun openAccountVerification() {
        navigateTo(ScreenDestination.AccountVerification)
    }

    fun setAccountVerified(method: String, target: String) {
        val toast = "Identity verified successfully via $method ($target)!"
        _uiState.update {
            it.copy(statusAdvanceToast = toast)
        }
        chatThreads.update { list ->
            val verificationNotice = com.example.data.model.ChatThread(
                id = "thread_verify_${System.currentTimeMillis()}",
                contactName = "🛡️ Security & Identity Verified",
                contactRole = "Official Brage Verification",
                contactInitials = "🛡️",
                contextTitle = "SMS / Email Code Confirmed",
                phoneNumber = "1-800-555-BRAGE",
                lastMessage = "Your account was successfully verified via $method. Verified Trust Badge active.",
                timestamp = "Just now",
                unreadCount = 1,
                isOnline = true,
                isSupport = true,
                category = "Notifications"
            )
            listOf(verificationNotice) + list
        }
    }

    fun openSettings() {
        navigateTo(ScreenDestination.Settings)
    }

    fun showToast(msg: String) {
        _uiState.update { it.copy(statusAdvanceToast = msg) }
    }
}


