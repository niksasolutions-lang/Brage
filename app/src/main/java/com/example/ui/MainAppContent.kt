package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.ServiceAddOn
import com.example.data.model.ServiceItem
import com.example.ui.components.DeilaPlayIcon
import com.example.ui.screens.*
import com.example.ui.viewmodel.BrageViewModel
import com.example.ui.viewmodel.NavigationTab
import com.example.ui.viewmodel.ScreenDestination
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrageAppContent(
    viewModel: BrageViewModel,
    isGuest: Boolean = false,
    onRequestSignIn: () -> Unit = {},
    onSignOut: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val displayedServices by viewModel.displayedServices.collectAsStateWithLifecycle()
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val addresses by viewModel.addresses.collectAsStateWithLifecycle()
    val savedCards by viewModel.savedCards.collectAsStateWithLifecycle()
    val providerApplications by viewModel.providerApplications.collectAsStateWithLifecycle()
    val providerReviewsMap by viewModel.providerReviews.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentServiceLocation.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val isDownloading by viewModel.isDownloading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val chatThreads by viewModel.chatThreads.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var bookingTargetService by remember { mutableStateOf<ServiceItem?>(null) }
    var bookingPreSelectedAddOns by remember { mutableStateOf<List<ServiceAddOn>>(emptyList()) }

    // Reschedule state
    var rescheduleDate by remember { mutableStateOf("Tomorrow, Oct 11") }
    var rescheduleTime by remember { mutableStateOf("2:00 PM - 5:00 PM") }

    // Review state
    var reviewRating by remember { mutableStateOf(5) }
    var reviewText by remember { mutableStateOf("") }

    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(5000)
        showSplash = false
    }

    val currentScreen = uiState.currentScreen
    val hideBottomBar = currentScreen is ScreenDestination.CallScreen ||
            currentScreen is ScreenDestination.VideoCallScreen ||
            currentScreen is ScreenDestination.VideoPlay ||
            currentScreen is ScreenDestination.DeilaPlayScreen ||
            currentScreen is ScreenDestination.ShortPage ||
            currentScreen is ScreenDestination.ChatScreen ||
            currentScreen is ScreenDestination.CardApplication ||
            currentScreen is ScreenDestination.ApplyProvider ||
            currentScreen is ScreenDestination.MapLocation ||
            currentScreen is ScreenDestination.EditProfilePicture ||
            currentScreen is ScreenDestination.ViewProfilePicture ||
            currentScreen is ScreenDestination.EditUsername ||
            currentScreen is ScreenDestination.AccountVerification ||
            currentScreen is ScreenDestination.Settings ||
            currentScreen is ScreenDestination.ProviderProfile ||
            currentScreen is ScreenDestination.ServiceDetail

    val totalUnreadChats = remember(chatThreads) { chatThreads.sumOf { it.unreadCount } }

    Scaffold(
        bottomBar = {
            if (!hideBottomBar) {
                NavigationBar(
                    modifier = Modifier.testTag("main_navigation_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = uiState.currentTab == NavigationTab.HOME && currentScreen is ScreenDestination.Home,
                        onClick = { viewModel.selectTab(NavigationTab.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        modifier = Modifier.testTag("nav_tab_home")
                    )

                    NavigationBarItem(
                        selected = uiState.currentTab == NavigationTab.CHAT || currentScreen is ScreenDestination.ChatHub,
                        onClick = { viewModel.selectTab(NavigationTab.CHAT) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (totalUnreadChats > 0) {
                                        Badge { Text("$totalUnreadChats") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ChatBubble, contentDescription = "Chat")
                            }
                        },
                        label = { Text("Chat") },
                        modifier = Modifier.testTag("nav_tab_chat")
                    )

                    // DEILA PLAY Center Tab (Pulsates and glows neon cyan when busy/loading/downloading/refreshing)
                    NavigationBarItem(
                        selected = uiState.currentTab == NavigationTab.DEILA_PLAY || currentScreen is ScreenDestination.DeilaPlay,
                        onClick = { viewModel.selectTab(NavigationTab.DEILA_PLAY) },
                        icon = {
                            DeilaPlayIcon(
                                isBusy = isLoadingMore || isDownloading || isRefreshing,
                                isSelected = uiState.currentTab == NavigationTab.DEILA_PLAY || currentScreen is ScreenDestination.DeilaPlay,
                                size = 24.dp
                            )
                        },
                        label = {
                            Text(
                                text = "DEILA PLAY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isLoadingMore || isDownloading || isRefreshing) {
                                    Color(0xFF00E5FF)
                                } else if (uiState.currentTab == NavigationTab.DEILA_PLAY || currentScreen is ScreenDestination.DeilaPlay) {
                                    Color(0xFF0A4A8A)
                                } else {
                                    Color(0xFF5A5A5A)
                                }
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_deila_play")
                    )

                    NavigationBarItem(
                        selected = uiState.currentTab == NavigationTab.BOOKINGS || currentScreen is ScreenDestination.BookingsList,
                        onClick = { viewModel.selectTab(NavigationTab.BOOKINGS) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    val activeCount = bookings.count { it.status == "CONFIRMED" || it.status == "IN_PROGRESS" || it.status == "EN_ROUTE" }
                                    if (activeCount > 0) {
                                        Badge { Text("$activeCount") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Bookings")
                            }
                        },
                        label = { Text("Bookings") },
                        modifier = Modifier.testTag("nav_tab_bookings")
                    )

                    NavigationBarItem(
                        selected = uiState.currentTab == NavigationTab.PROFILE || currentScreen is ScreenDestination.ProfileAndSupport,
                        onClick = { viewModel.selectTab(NavigationTab.PROFILE) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        modifier = Modifier.testTag("nav_tab_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is ScreenDestination.Home -> {
                    HomeScreen(
                        categories = viewModel.categories,
                        displayedServices = displayedServices,
                        favorites = favorites,
                        activeBookings = bookings,
                        searchQuery = uiState.searchQuery,
                        activeFilter = uiState.activeFilter,
                        currentLocation = currentLocation,
                        isLoadingMore = isLoadingMore,
                        onLoadMore = { viewModel.loadMoreServices() },
                        onOpenMapLocation = { viewModel.openMapLocation() },
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onFilterChange = { viewModel.setFilter(it) },
                        onCategoryClick = { viewModel.selectCategory(it) },
                        onViewAllCategoriesClick = { viewModel.navigateTo(ScreenDestination.AllCategories) },
                        onServiceClick = { viewModel.navigateTo(ScreenDestination.ServiceDetail(it.id)) },
                        onBookServiceClick = {
                            bookingTargetService = it
                            bookingPreSelectedAddOns = emptyList()
                        },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                        onCallProvider = { name, role, initials, contextTitle, phone ->
                            viewModel.startCall(name, role, initials, contextTitle, phone)
                        },
                        onViewBookingsClick = { viewModel.selectTab(NavigationTab.BOOKINGS) },
                        onOpenNotificationsToChat = { viewModel.selectTab(NavigationTab.CHAT) }
                    )
                }

                is ScreenDestination.ChatHub -> {
                    ChatHubScreen(
                        threads = chatThreads,
                        activeBookings = bookings,
                        availableServices = viewModel.allServices,
                        onSelectThread = { thread ->
                            viewModel.startChat(
                                name = thread.contactName,
                                role = thread.contactRole,
                                initials = thread.contactInitials,
                                contextTitle = thread.contextTitle,
                                phone = thread.phoneNumber
                            )
                        },
                        onStartNewChat = { name, role, initials, contextTitle, phone ->
                            viewModel.startChat(name, role, initials, contextTitle, phone)
                        },
                        onCallProvider = { name, role, initials, contextTitle, phone ->
                            viewModel.startCall(name, role, initials, contextTitle, phone)
                        },
                        onOpenDeilaAi = { viewModel.selectTab(NavigationTab.DEILA_AI) }
                    )
                }

                is ScreenDestination.AllCategories -> {
                    AllCategoriesScreen(
                        categories = viewModel.categories,
                        allServices = viewModel.allServices,
                        onCategoryClick = { viewModel.selectCategory(it) }
                    )
                }

                is ScreenDestination.CategoryDetail -> {
                    val cat = viewModel.repository.getCategoryById(screen.categoryId)
                    if (cat != null) {
                        CategoryDetailScreen(
                            category = cat,
                            services = viewModel.repository.getServicesByCategory(cat.id),
                            favorites = favorites,
                            onBackClick = { viewModel.navigateBack() },
                            onServiceClick = { viewModel.navigateTo(ScreenDestination.ServiceDetail(it.id)) },
                            onBookServiceClick = {
                                bookingTargetService = it
                                bookingPreSelectedAddOns = emptyList()
                            },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            onCallProvider = { name, role, initials, contextTitle, phone ->
                                viewModel.startCall(name, role, initials, contextTitle, phone)
                            }
                        )
                    } else {
                        viewModel.selectTab(NavigationTab.HOME)
                    }
                }

                is ScreenDestination.ServiceDetail -> {
                    val srv = viewModel.repository.getServiceById(screen.serviceId)
                    if (srv != null) {
                        ServiceDetailScreen(
                            service = srv,
                            isFavorite = favorites.contains(srv.id),
                            onBackClick = { viewModel.navigateBack() },
                            onFavoriteToggle = { viewModel.toggleFavorite(srv.id) },
                            onBookClick = { addOns ->
                                bookingTargetService = srv
                                bookingPreSelectedAddOns = addOns
                            },
                            onCallProvider = { name, role, initials, contextTitle, phone ->
                                viewModel.startCall(name, role, initials, contextTitle, phone)
                            },
                            onViewProviderProfile = { providerId ->
                                viewModel.openProviderProfile(providerId)
                            }
                        )
                    } else {
                        viewModel.navigateBack()
                    }
                }

                is ScreenDestination.ProviderProfile -> {
                    val srv = viewModel.allServices.find { it.provider.id == screen.providerId } ?: viewModel.allServices.first()
                    val pReviews = providerReviewsMap[screen.providerId] ?: viewModel.getReviewsForProvider(screen.providerId)
                    val pCompletedBookings = bookings.filter {
                        it.status == "COMPLETED" && (
                            it.serviceId.startsWith(screen.providerId) ||
                            it.providerName.equals(srv.provider.name, ignoreCase = true)
                        )
                    }
                    ProviderProfileScreen(
                        provider = srv.provider,
                        servicesOffered = viewModel.allServices.filter { it.provider.id == screen.providerId }.ifEmpty { listOf(srv) },
                        reviews = pReviews,
                        completedBookings = pCompletedBookings,
                        onBackClick = { viewModel.navigateBack() },
                        onCallProvider = { name, role, initials, contextTitle, phone ->
                            viewModel.startCall(name, role, initials, contextTitle, phone)
                        },
                        onChatProvider = { name, role, initials, contextTitle, phone ->
                            viewModel.startChat(name, role, initials, contextTitle, phone)
                        },
                        onBookService = {
                            bookingTargetService = it
                            bookingPreSelectedAddOns = emptyList()
                        },
                        onSubmitReview = { author, rating, comment, srvTitle, tags, wouldRec, bookingId ->
                            viewModel.submitProviderReview(
                                providerId = screen.providerId,
                                authorName = author,
                                rating = rating,
                                comment = comment,
                                serviceBooked = srvTitle,
                                tags = tags,
                                wouldRecommend = wouldRec,
                                bookingId = bookingId
                            )
                        },
                        onHelpfulReview = { reviewId ->
                            viewModel.toggleHelpfulReview(screen.providerId, reviewId)
                        }
                    )
                }

                is ScreenDestination.DeilaPlay -> {
                    EntertainmentScreen(
                        isLoading = isLoadingMore,
                        isDownloading = isDownloading,
                        isRefreshing = isRefreshing,
                        onTriggerRefresh = { viewModel.triggerRefresh() },
                        onTriggerDownload = { videoId, title, channel, thumbUrl ->
                            viewModel.triggerDownload(videoId, title, channel, thumbUrl)
                        },
                        onOpenDeilaAiChat = { viewModel.navigateTo(ScreenDestination.DeilaAi) },
                        onBackClick = { viewModel.navigateBack() },
                        onOpenVideoPlay = { item ->
                            if (item.id == "synth_neon") {
                                viewModel.navigateTo(
                                    ScreenDestination.DeilaPlayScreen(
                                        videoId = item.id,
                                        videoTitle = item.title,
                                        channelInfo = "${item.channelName} • ${item.views} • ${item.uploadTime}"
                                    )
                                )
                            } else {
                                viewModel.openVideoPlay(
                                    videoId = item.id,
                                    videoTitle = item.title,
                                    videoCategory = item.category,
                                    videoDuration = item.duration,
                                    creatorName = item.channelName
                                )
                            }
                        },
                        onOpenDeilaPlayFinal = { item ->
                            viewModel.navigateTo(
                                ScreenDestination.DeilaPlayScreen(
                                    videoId = item.id,
                                    videoTitle = item.title,
                                    channelInfo = "${item.channelName} • ${item.views} • ${item.uploadTime}"
                                )
                            )
                        },
                        onOpenShortPage = { shortId ->
                            viewModel.openShortPage(shortId)
                        },
                        onOpenDownloadedVideo = { video ->
                            viewModel.openDownloadedVideo(video)
                        },
                        downloadedVideos = viewModel.downloadedVideos.collectAsStateWithLifecycle().value,
                        downloadProgress = uiState.downloadProgress,
                        playlists = viewModel.playlists.collectAsStateWithLifecycle().value,
                        onCreatePlaylist = { name -> viewModel.createPlaylist(name) },
                        onDeletePlaylist = { id -> viewModel.deletePlaylist(id) },
                        onAddVideoToPlaylist = { playlistId, videoId -> viewModel.addVideoToPlaylist(playlistId, videoId) }
                    )
                }

                is ScreenDestination.DeilaPlayScreen -> {
                    DeilaPlayScreen(
                        videoId = screen.videoId,
                        videoTitle = screen.videoTitle,
                        channelInfo = screen.channelInfo,
                        playlists = viewModel.playlists.collectAsStateWithLifecycle().value,
                        onSaveToPlaylist = { playlistId, videoId ->
                            viewModel.addVideoToPlaylist(playlistId, videoId)
                        },
                        onCreatePlaylist = { name ->
                            viewModel.createPlaylist(name)
                        },
                        onBackClick = { viewModel.navigateBack() }
                    )
                }

                is ScreenDestination.ShortPage -> {
                    ShortPageScreen(
                        initialShortId = screen.initialShortId,
                        onBackClick = { viewModel.navigateBack() },
                        onOpenVideoPlay = { videoId ->
                            viewModel.openVideoPlay(videoId = videoId)
                        }
                    )
                }

                is ScreenDestination.DownloadedVideoPlay -> {
                    DownloadedVideoPlayScreen(
                        video = screen.video,
                        onBack = { viewModel.navigateBack() }
                    )
                }

                is ScreenDestination.VideoPlay -> {
                    VideoPlayScreen(
                        videoId = screen.videoId,
                        videoTitle = screen.videoTitle,
                        videoCategory = screen.videoCategory,
                        videoDuration = screen.videoDuration,
                        creatorName = screen.creatorName,
                        initialTimestampSeconds = screen.initialTimestampSeconds,
                        isDownloading = isDownloading,
                        onTriggerDownload = { 
                            viewModel.triggerDownload(
                                videoId = screen.videoId,
                                title = screen.videoTitle,
                                channel = screen.creatorName,
                                thumbUrl = "https://picsum.photos/400/800?1" // Placeholder thumbUrl
                            ) 
                        },
                        onBackClick = { viewModel.navigateBack() },
                        onOpenDeilaAiChat = { viewModel.navigateTo(ScreenDestination.DeilaAi) }
                    )
                }

                is ScreenDestination.DeilaAi -> {
                    DeilaAiScreen(
                        allServices = viewModel.allServices,
                        onSelectService = { viewModel.navigateTo(ScreenDestination.ServiceDetail(it.id)) },
                        onBookService = {
                            bookingTargetService = it
                            bookingPreSelectedAddOns = emptyList()
                        },
                        onCallProvider = { name, role, initials, contextTitle, phone ->
                            viewModel.startCall(name, role, initials, contextTitle, phone)
                        },
                        onChatProvider = { name, role, initials, contextTitle, phone ->
                            viewModel.startChat(name, role, initials, contextTitle, phone)
                        },
                        onOpenProfilePicView = {
                            viewModel.openViewProfilePicture(isDeilaAiView = true, customTitle = "Deila AI Profile Picture")
                        }
                    )
                }

                is ScreenDestination.Contract -> {
                    ServiceContractScreen(
                        onBackClick = { viewModel.navigateBack() },
                        onAcceptAndSign = { viewModel.navigateBack() }
                    )
                }

                is ScreenDestination.Application -> {
                    ApplicationScreen(
                        onBackClick = { viewModel.navigateBack() },
                        onSubmitClick = { name, service ->
                            viewModel.navigateBack()
                        }
                    )
                }

                is ScreenDestination.CallScreen -> {
                    CallScreen(
                        contactName = screen.contactName,
                        contactRole = screen.contactRole,
                        contactInitials = screen.contactInitials,
                        contextTitle = screen.contextTitle,
                        phoneNumber = screen.phoneNumber,
                        onEndCall = { viewModel.navigateBack() },
                        onSwitchToVideoCall = {
                            viewModel.startVideoCall(
                                screen.contactName,
                                screen.contactRole,
                                screen.contactInitials,
                                screen.contextTitle,
                                screen.phoneNumber
                            )
                        }
                    )
                }

                is ScreenDestination.VideoCallScreen -> {
                    VideoCallScreen(
                        contactName = screen.contactName,
                        contactRole = screen.contactRole,
                        contactInitials = screen.contactInitials,
                        contextTitle = screen.contextTitle,
                        phoneNumber = screen.phoneNumber,
                        onEndCall = { viewModel.navigateBack() },
                        onSwitchToAudioCall = {
                            viewModel.startCall(
                                screen.contactName,
                                screen.contactRole,
                                screen.contactInitials,
                                screen.contextTitle,
                                screen.phoneNumber
                            )
                        },
                        onBookService = {
                            viewModel.selectTab(NavigationTab.HOME)
                        }
                    )
                }

                is ScreenDestination.ChatScreen -> {
                    ChatScreen(
                        contactName = screen.contactName,
                        contactRole = screen.contactRole,
                        contactInitials = screen.contactInitials,
                        contextTitle = screen.contextTitle,
                        phoneNumber = screen.phoneNumber,
                        onBackClick = { viewModel.navigateBack() },
                        onCallClick = {
                            viewModel.startCall(
                                screen.contactName,
                                screen.contactRole,
                                screen.contactInitials,
                                screen.contextTitle,
                                screen.phoneNumber
                            )
                        },
                        onVideoCallClick = {
                            viewModel.startVideoCall(
                                screen.contactName,
                                screen.contactRole,
                                screen.contactInitials,
                                screen.contextTitle,
                                screen.phoneNumber
                            )
                        }
                    )
                }

                is ScreenDestination.BookingsList -> {
                    BookingsScreen(
                        bookings = bookings,
                        onAdvanceStatus = { viewModel.advanceBookingStatus(it) },
                        onCancelBooking = { viewModel.cancelBooking(it) },
                        onOpenReschedule = { viewModel.openRescheduleModal(it) },
                        onOpenReview = { viewModel.openReviewModal(it) },
                        onCallPro = { name, role, initials, contextTitle, phone ->
                            viewModel.startCall(name, role, initials, contextTitle, phone)
                        },
                        onChatPro = { name, role, initials, contextTitle, phone ->
                            viewModel.startChat(name, role, initials, contextTitle, phone)
                        }
                    )
                }

                is ScreenDestination.SavedServices -> {
                    val saved = viewModel.allServices.filter { favorites.contains(it.id) }
                    SavedScreen(
                        savedServices = saved,
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                        onServiceClick = { viewModel.navigateTo(ScreenDestination.ServiceDetail(it.id)) },
                        onBookServiceClick = {
                            bookingTargetService = it
                            bookingPreSelectedAddOns = emptyList()
                        },
                        onCallProvider = { name, role, initials, contextTitle, phone ->
                            viewModel.startCall(name, role, initials, contextTitle, phone)
                        }
                    )
                }

                is ScreenDestination.ProProfile -> {
                    ProProfileScreen(
                        onBackClick = { viewModel.navigateBack() },
                        onBookNowClick = { viewModel.navigateBack() }
                    )
                }

                is ScreenDestination.ProfileAndSupport -> {
                    ProfileScreen(
                        savedAddresses = addresses,
                        savedCardsCount = savedCards.size,
                        providerApplications = providerApplications,
                        userProfile = userProfile,
                        bookings = bookings,
                        isGuest = isGuest,
                        onRequestSignIn = onRequestSignIn,
                        onAddAddress = { label, street, cityStateZip ->
                            viewModel.addAddress(label, street, cityStateZip)
                        },
                        onViewSavedServices = { viewModel.navigateTo(ScreenDestination.SavedServices) },
                        onOpenCardApplication = { viewModel.openCardApplication() },
                        onOpenApplyProvider = { viewModel.openApplyProvider() },
                        onOpenEditProfilePicture = { viewModel.openEditProfilePicture() },
                        onOpenViewProfilePicture = { viewModel.openViewProfilePicture() },
                        onOpenEditUsername = { viewModel.openEditUsername() },
                        onOpenAccountVerification = { viewModel.openAccountVerification() },
                        onOpenSettings = { viewModel.openSettings() },
                        onCallSupport = {
                            viewModel.startCall(
                                "Brage Concierge Support",
                                "24/7 Hotline",
                                "BC",
                                "Emergency Assistance & Guarantee",
                                "+1 (800) 555-BRAGE"
                            )
                        },
                        onChatSupport = {
                            viewModel.startChat(
                                "Brage Concierge Support",
                                "24/7 Hotline",
                                "BC",
                                "General Assistance",
                                "+1 (800) 555-BRAGE"
                            )
                        },
                        onSignOut = onSignOut,
                        onRateBooking = { bookingId, rating, comment ->
                            viewModel.submitReview(bookingId, rating, comment)
                        },
                        onCompleteSignature = { appId, sig ->
                            viewModel.completeApplicationSignature(appId, sig)
                        }
                    )
                }

                is ScreenDestination.Settings -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onBackClick = { viewModel.navigateBack() },
                        onShowToast = { msg -> viewModel.showToast(msg) }
                    )
                }

                is ScreenDestination.AccountVerification -> {
                    AccountVerificationScreen(
                        userProfile = userProfile,
                        onVerificationSuccess = { method, target ->
                            viewModel.setAccountVerified(method, target)
                        },
                        onBackClick = { viewModel.navigateBack() }
                    )
                }

                is ScreenDestination.EditProfilePicture -> {
                    EditProfilePictureScreen(
                        currentProfile = userProfile,
                        onSaveProfilePicture = { uri, preset, colorHex ->
                            viewModel.updateProfilePicture(uri, preset, colorHex)
                        },
                        onViewFullScreenClick = { viewModel.openViewProfilePicture() },
                        onBackClick = { viewModel.navigateBack() }
                    )
                }

                is ScreenDestination.ViewProfilePicture -> {
                    val displayProfile = if (screen.isDeilaAiView) {
                        userProfile.copy(
                            fullName = "Deila AI Concierge",
                            username = "deila.ai",
                            profilePhotoUri = "deila_logo_ai"
                        )
                    } else {
                        userProfile
                    }
                    ProfilePicPageViewScreen(
                        userProfile = displayProfile,
                        title = screen.customTitle ?: "Profile Picture",
                        onEditPhotoClick = { viewModel.openEditProfilePicture() },
                        onBackClick = { viewModel.navigateBack() }
                    )
                }

                is ScreenDestination.EditUsername -> {
                    EditUsernameScreen(
                        currentProfile = userProfile,
                        onSaveProfileInfo = { fullName, username, bio ->
                            viewModel.updateProfileInfo(fullName, username, bio)
                        },
                        onBackClick = { viewModel.navigateBack() }
                    )
                }

                is ScreenDestination.CardApplication -> {
                    CardApplicationScreen(
                        savedCards = savedCards,
                        onBackClick = { viewModel.navigateBack() },
                        onSaveCard = { viewModel.addSavedCard(it) },
                        onRemoveCard = { viewModel.removeSavedCard(it) }
                    )
                }

                is ScreenDestination.ApplyProvider -> {
                    ApplyProviderScreen(
                        categories = viewModel.categories,
                        onBackClick = { viewModel.navigateBack() },
                        onSubmitApplication = { viewModel.submitProviderApplication(it) }
                    )
                }

                is ScreenDestination.MapLocation -> {
                    MapLocationScreen(
                        currentLocation = currentLocation,
                        categories = viewModel.categories,
                        allServices = viewModel.allServices,
                        initialSelectedProviderId = screen.providerId,
                        onBackClick = { viewModel.navigateBack() },
                        onSaveLocation = { newLoc ->
                            viewModel.updateServiceLocation(newLoc)
                        },
                        onViewProviderProfile = { providerId ->
                            viewModel.openProviderProfile(providerId)
                        },
                        onBookService = { service ->
                            bookingTargetService = service
                            bookingPreSelectedAddOns = emptyList()
                        },
                        onCallProvider = { name, role, initials, contextTitle, phone ->
                            viewModel.startCall(name, role, initials, contextTitle, phone)
                        }
                    )
                }
                else -> {
                    viewModel.selectTab(NavigationTab.HOME)
                }
            }

            if (showSplash) {
                SplashScreen(onDismiss = { showSplash = false })
            }

            // Booking Flow Sheet
            bookingTargetService?.let { service ->
                BookingFlowSheet(
                    service = service,
                    preSelectedAddOns = bookingPreSelectedAddOns,
                    savedAddresses = addresses,
                    onDismiss = { bookingTargetService = null },
                    onConfirmBooking = { date, slot, address, phone, addOns, isExpress, notes ->
                        viewModel.createBooking(
                            service = service,
                            scheduledDate = date,
                            timeSlot = slot,
                            address = address,
                            phone = phone,
                            selectedAddOns = addOns,
                            isExpress = isExpress,
                            notes = notes
                        )
                        bookingTargetService = null
                    }
                )
            }

            // Booking Success Dialog
            uiState.bookingSuccessModal?.let { booked ->
                AlertDialog(
                    onDismissRequest = { viewModel.dismissSuccessModal() },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    title = {
                        Text("Booking Confirmed!", fontWeight = FontWeight.ExtraBold)
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Your appointment for ${booked.serviceTitle} has been scheduled successfully.")
                            Text("• Date: ${booked.scheduledDate} (${booked.scheduledTimeSlot})", fontWeight = FontWeight.SemiBold)
                            Text("• Specialist: ${booked.providerName}", fontWeight = FontWeight.SemiBold)
                            Text("• Total: $${booked.totalPrice.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Our specialist will arrive at the scheduled time. You can message or call them directly in the app anytime!")
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.dismissSuccessModal()
                                viewModel.selectTab(NavigationTab.BOOKINGS)
                            }
                        ) {
                            Text("Track in Bookings")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissSuccessModal() }) {
                            Text("Done")
                        }
                    }
                )
            }

            // Reschedule Dialog
            uiState.rescheduleTargetBooking?.let { target ->
                AlertDialog(
                    onDismissRequest = { viewModel.dismissRescheduleModal() },
                    title = { Text("Reschedule Service", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Current: ${target.scheduledDate} (${target.scheduledTimeSlot})")
                            OutlinedTextField(
                                value = rescheduleDate,
                                onValueChange = { rescheduleDate = it },
                                label = { Text("New Date") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = rescheduleTime,
                                onValueChange = { rescheduleTime = it },
                                label = { Text("New Time Slot") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.submitReschedule(target.id, rescheduleDate, rescheduleTime)
                            }
                        ) {
                            Text("Confirm Reschedule")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissRescheduleModal() }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Review Dialog
            uiState.reviewTargetBooking?.let { target ->
                AlertDialog(
                    onDismissRequest = { viewModel.dismissReviewModal() },
                    title = { Text("Rate & Review ${target.providerName}", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("How was your experience with ${target.serviceTitle}?")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                (1..5).forEach { star ->
                                    IconButton(onClick = { reviewRating = star }) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "$star stars",
                                            tint = if (star <= reviewRating) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outlineVariant,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = reviewText,
                                onValueChange = { reviewText = it },
                                label = { Text("Leave feedback for the specialist...") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 4
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.submitReview(target.id, reviewRating, reviewText)
                                reviewText = ""
                            }
                        ) {
                            Text("Submit Review")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissReviewModal() }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Status notification snackbar entering directly on Chat page
            uiState.statusAdvanceToast?.let { toast ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
                        .clickable {
                            viewModel.clearStatusToast()
                            viewModel.selectTab(NavigationTab.CHAT)
                        }
                        .testTag("notification_toast_to_chat"),
                    action = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = {
                                viewModel.clearStatusToast()
                                viewModel.selectTab(NavigationTab.CHAT)
                            }) {
                                Text("Open Chat", color = MaterialTheme.colorScheme.inversePrimary, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = { viewModel.clearStatusToast() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(toast, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
