package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BookingEntity
import com.example.data.model.CustomerTestimonial
import com.example.data.model.PortfolioItem
import com.example.data.model.ProviderInfo
import com.example.data.model.ServiceItem

data class ServicePackage(
    val id: String,
    val name: String,
    val price: Double,
    val durationText: String,
    val description: String,
    val features: List<String>,
    val isPopular: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProfileScreen(
    provider: ProviderInfo,
    servicesOffered: List<ServiceItem>,
    reviews: List<CustomerTestimonial> = emptyList(),
    completedBookings: List<BookingEntity> = emptyList(),
    onBackClick: () -> Unit,
    onCallProvider: (name: String, role: String, initials: String, contextTitle: String, phone: String) -> Unit,
    onChatProvider: (name: String, role: String, initials: String, contextTitle: String, phone: String) -> Unit,
    onBookService: (ServiceItem) -> Unit,
    onSubmitReview: (authorName: String, rating: Int, comment: String, serviceBooked: String, tags: List<String>, wouldRecommend: Boolean, bookingId: Long?) -> Unit = { _, _, _, _, _, _, _ -> },
    onHelpfulReview: (reviewId: String) -> Unit = {}
) {
    BackHandler {
        onBackClick()
    }

    val primaryService = remember(servicesOffered) {
        servicesOffered.firstOrNull() ?: ServiceItem(
            id = "${provider.id}_default_srv",
            categoryId = "home_cleaning",
            title = "${provider.title} Consultation & Work",
            subtitle = "Professional on-demand service by ${provider.name}",
            basePrice = provider.hourlyRate,
            priceUnit = "/ hr",
            rating = provider.rating,
            reviewCount = provider.reviewCount,
            durationMinutes = 90,
            description = provider.bio,
            includedHighlights = listOf("Full inspection & labor", "Commercial gear", "30-Day Guarantee"),
            provider = provider
        )
    }

    // Dynamic Portfolio Items customized for the provider's domain
    val portfolioItems = remember(provider.id) {
        listOf(
            PortfolioItem(
                id = "${provider.id}_port_1",
                title = "Complete Residential Overhaul & Sanitization",
                category = "Full Scope",
                description = "Floor-to-ceiling execution with commercial HEPA extraction, deep detail restoration, and surface seal coating.",
                completionDate = "Oct 4, 2026",
                durationHours = 3.5,
                rating = 5.0,
                beforeState = "Heavy buildup, high-traffic grime, and compromised seals across all key areas.",
                afterState = "100% pristine showroom finish with anti-microbial barrier protection applied.",
                tags = listOf("⭐ 5.0 Client Rating", "⚡ Completed in 3.5h", "🧼 100% Spotless"),
                costEstimate = "$180 - $240"
            ),
            PortfolioItem(
                id = "${provider.id}_port_2",
                title = "Emergency Diagnostics & High-Precision Repair",
                category = "Diagnostics",
                description = "Rapid response troubleshooting, isolating electrical and hardware bottlenecks with immediate part replacement.",
                completionDate = "Sep 28, 2026",
                durationHours = 2.0,
                rating = 5.0,
                beforeState = "System tripped safety switch with intermittent failure and non-functional control units.",
                afterState = "Calibrated, load-tested, and certified under safety compliance standards.",
                tags = listOf("⚡ Express Dispatch", "🔧 Master Craftsmanship", "🛡️ 90-Day Warranty"),
                costEstimate = "$120 - $160"
            ),
            PortfolioItem(
                id = "${provider.id}_port_3",
                title = "Detailed Surface Restoration & Protective Treatment",
                category = "Restoration",
                description = "Specialized multi-stage surface polishing, micro-abrasion removal, and weather-resistant polymer application.",
                completionDate = "Sep 19, 2026",
                durationHours = 4.0,
                rating = 4.9,
                beforeState = "Oxidized layer and surface scratches from extensive seasonal weathering.",
                afterState = "Mirror-gloss finish with hydrophobic nano-ceramic protective seal.",
                tags = listOf("💎 Premium Finish", "🛡️ High Durability", "🤝 Client Verified"),
                costEstimate = "$220 - $290"
            ),
            PortfolioItem(
                id = "${provider.id}_port_4",
                title = "Routine Maintenance & Preventive Inspection",
                category = "Maintenance",
                description = "Multi-point inspection, lubrication of moving mechanisms, filter renewal, and efficiency performance testing.",
                completionDate = "Sep 10, 2026",
                durationHours = 1.5,
                rating = 5.0,
                beforeState = "Overdue for scheduled checkup, minor vibration and efficiency loss.",
                afterState = "Optimal operating tolerance verified with digital performance log.",
                tags = listOf("⏱️ Fast Turnaround", "💰 Cost-Saver", "📜 Certified Report"),
                costEstimate = "$95 - $130"
            )
        )
    }

    // Provider Package Tiers
    val packages = remember(provider.id, primaryService.basePrice) {
        listOf(
            ServicePackage(
                id = "pkg_basic",
                name = "Essential Service Package",
                price = primaryService.basePrice,
                durationText = "~1 - 2 Hours",
                description = "Standard maintenance, inspection, and essential on-site diagnostics for immediate needs.",
                features = listOf(
                    "Standard inspection & diagnostic check",
                    "All labor and standard equipment included",
                    "Brage 30-Day Quality Guarantee"
                ),
                isPopular = false
            ),
            ServicePackage(
                id = "pkg_deluxe",
                name = "Comprehensive Pro Package",
                price = primaryService.basePrice * 1.35,
                durationText = "~3 - 4 Hours",
                description = "Full execution with advanced deep treatment, heavy equipment, and extended satisfaction warranty.",
                features = listOf(
                    "Everything in Essential Package",
                    "High-grade commercial deep treatment",
                    "Extended 90-Day Satisfaction Warranty",
                    "Complimentary priority follow-up visit"
                ),
                isPopular = true
            )
        )
    }

    // Local Testimonials Fallback
    val defaultTestimonials = remember(provider.id) {
        listOf(
            CustomerTestimonial(
                id = "${provider.id}_t1",
                providerId = provider.id,
                authorName = "Marcus Aurelius",
                rating = 5,
                dateText = "3 days ago",
                serviceBooked = primaryService.title,
                comment = "Absolutely exceptional service from ${provider.name}. Arrived exactly on the minute, brought top-tier gear, and did a flawless job without cutting any corners. Highly recommend!",
                isVerified = true,
                helpfulCount = 8,
                tags = listOf("⚡ Punctual & On-Time", "🧼 Left Area Spotless", "💯 Quality Guaranteed"),
                wouldRecommend = true
            ),
            CustomerTestimonial(
                id = "${provider.id}_t2",
                providerId = provider.id,
                authorName = "Samantha Miller",
                rating = 5,
                dateText = "1 week ago",
                serviceBooked = primaryService.title,
                comment = "Professional, polite, and respectful of our home. Clear upfront communication on price with zero surprise charges. Will book again!",
                isVerified = true,
                helpfulCount = 4,
                tags = listOf("💬 Clear Communication", "💰 Fair Upfront Pricing"),
                wouldRecommend = true
            ),
            CustomerTestimonial(
                id = "${provider.id}_t3",
                providerId = provider.id,
                authorName = "David K.",
                rating = 4,
                dateText = "2 weeks ago",
                serviceBooked = primaryService.title,
                comment = "Very knowledgeable and resolved the issue quickly. Great attention to detail and gave solid advice on preventative care.",
                isVerified = true,
                helpfulCount = 2,
                tags = listOf("🔧 Expert Craftsmanship", "⏱️ Fast & Efficient"),
                wouldRecommend = true
            )
        )
    }

    val activeReviews = if (reviews.isNotEmpty()) reviews else defaultTestimonials

    // Review Sheet, Filter States & Portfolio Detail Modal
    var showReviewSheet by remember { mutableStateOf(false) }
    var ratingFilter by remember { mutableIntStateOf(0) } // 0 = All, 5 = 5★, 4 = 4★, 3 = 3★
    var sortMode by remember { mutableIntStateOf(0) } // 0 = Most Recent, 1 = Highest Rating
    var reviewSubmitSuccessMessage by remember { mutableStateOf<String?>(null) }
    var selectedPortfolioItem by remember { mutableStateOf<PortfolioItem?>(null) }
    var portfolioCategoryFilter by remember { mutableStateOf("All") }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = provider.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Verified Specialist • ${provider.title}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Share button
                    IconButton(
                        onClick = { /* Share provider profile */ },
                        modifier = Modifier.testTag("btn_share_provider")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Provider",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Sticky Persistent Bottom CTA Bar with Hourly Rate and "Book Now"
            Surface(
                shadowElevation = 12.dp,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$${provider.hourlyRate.toInt()}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " / hr",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Available Today • Instant Dispatch",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF047857),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = { onBookService(primaryService) },
                        modifier = Modifier
                            .height(52.dp)
                            .widthIn(min = 170.dp)
                            .testTag("provider_detail_book_now_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Book Now",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("provider_profile_screen"),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Hero Banner with Avatar & Verified Badge
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        // Avatar Badge with Glowing Effect
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(92.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    )
                                    .border(3.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = provider.initials,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }

                            // Verified Checkmark Pill
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF10B981),
                                shadowElevation = 3.dp,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Verified Specialist",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Business Name & Title
                        Text(
                            text = provider.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "${provider.title} • ${provider.badge}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Rating & Location Tag
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${provider.rating} (${provider.reviewCount} reviews)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${provider.locationName} (${provider.distanceMiles} mi)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 4 Key Quick-Stats Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatBox(
                                title = "Experience",
                                value = "${provider.experienceYears}+ Yrs",
                                icon = Icons.Default.WorkHistory,
                                modifier = Modifier.weight(1f)
                            )
                            StatBox(
                                title = "Jobs Done",
                                value = "${provider.completedJobs}+",
                                icon = Icons.Default.TaskAlt,
                                modifier = Modifier.weight(1f)
                            )
                            StatBox(
                                title = "Response",
                                value = "< 15 min",
                                icon = Icons.Default.Bolt,
                                modifier = Modifier.weight(1f)
                            )
                            StatBox(
                                title = "Guarantee",
                                value = "100%",
                                icon = Icons.Default.Shield,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Call & Chat
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    onCallProvider(
                                        provider.name,
                                        provider.title,
                                        provider.initials,
                                        primaryService.title,
                                        provider.phoneNumber
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("profile_call_pro_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call Specialist", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    onChatProvider(
                                        provider.name,
                                        provider.title,
                                        provider.initials,
                                        primaryService.title,
                                        provider.phoneNumber
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("profile_chat_pro_button")
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Message Chat", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 1. Professional Bio & Credentials Section
            item {
                SectionCard(
                    title = "Professional Bio & Qualifications",
                    icon = Icons.Default.Badge
                ) {
                    Text(
                        text = provider.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Verified Credentials & Trust Guarantees",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        "Licensed Specialist • ID #LIC-${provider.id.take(6).uppercase()}-OR",
                        "Clean Background Check • Cleared via National Database (Updated Oct 2026)",
                        "100% Bonded & Insured up to $2,000,000 for residential & commercial sites",
                        "Equipped with certified commercial-grade gear & eco-safe materials",
                        "Covered under the Brage 30-Day Workmanship Satisfaction Guarantee"
                    ).forEach { cert ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cert,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // 2. Hourly Rates & Pricing Structure Section
            item {
                SectionCard(
                    title = "Hourly Rates & Transparent Pricing",
                    icon = Icons.Default.Sell
                ) {
                    // Hourly Rate Spotlight Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Standard Hourly Rate",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "FIXED RATE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Transparent hourly billing • Minimum 1.5 hr job booking. No hidden trip fees within 20 miles.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$${provider.hourlyRate.toInt()}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "/ hour",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Rate Breakdown Grid
                    Text(
                        text = "Rate Breakdown & Dispatch Types",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    RateBreakdownRow(
                        type = "Scheduled On-Site Work",
                        rate = "$${provider.hourlyRate.toInt()} / hr",
                        detail = "Standard booked jobs during normal working hours (Mon-Sat)"
                    )
                    RateBreakdownRow(
                        type = "Emergency & Express Dispatch",
                        rate = "$${(provider.hourlyRate * 1.3).toInt()} / hr",
                        detail = "Priority dispatch with arrival in < 45 minutes"
                    )
                    RateBreakdownRow(
                        type = "Diagnostics & Scope Assessment",
                        rate = "FREE with booking",
                        detail = "Waived diagnostic check fee when proceeding with service"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Curated All-Inclusive Service Packages",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Package cards
                    packages.forEach { pkg ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (pkg.isPopular) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                            ),
                            border = if (pkg.isPopular) BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary) else null,
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = pkg.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (pkg.isPopular) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.secondary
                                        ) {
                                            Text(
                                                text = "MOST POPULAR",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "$${pkg.price.toInt()}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = " • ${pkg.durationText}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }

                                Text(
                                    text = pkg.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )

                                pkg.features.forEach { feat ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(feat, style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { onBookService(primaryService) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("book_package_${pkg.id}"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Book Package • $${pkg.price.toInt()}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Portfolio Gallery Section
            item {
                SectionCard(
                    title = "Portfolio Gallery & Recent Work",
                    icon = Icons.Default.Collections
                ) {
                    Text(
                        text = "Browse real completed jobs, before & after transformations, and verified client outcomes by ${provider.name}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Portfolio category filter tabs
                    val portfolioCategories = listOf("All", "Full Scope", "Diagnostics", "Restoration", "Maintenance")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(portfolioCategories) { cat ->
                            FilterChip(
                                selected = portfolioCategoryFilter == cat,
                                onClick = { portfolioCategoryFilter = cat },
                                label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val filteredPortfolio = remember(portfolioItems, portfolioCategoryFilter) {
                        if (portfolioCategoryFilter == "All") portfolioItems
                        else portfolioItems.filter { it.category == portfolioCategoryFilter }
                    }

                    // Portfolio Cards
                    filteredPortfolio.forEach { portItem ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { selectedPortfolioItem = portItem }
                                .testTag("portfolio_item_${portItem.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = portItem.category.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = portItem.completionDate,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = portItem.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = portItem.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Before & After Comparison Pill
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFEF4444))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = portItem.beforeState,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF10B981))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = portItem.afterState,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF047857)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Tags & View details prompt
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        portItem.tags.take(2).forEach { tag ->
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Text(
                                                    text = tag,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 9.sp,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "View Case Study",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Completed Service Detected Notification (if customer completed a booking with this pro)
            if (completedBookings.isNotEmpty()) {
                val latestCompleted = completedBookings.first()
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .testTag("completed_service_feedback_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Service Completed with ${provider.name}!",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF065F46)
                                    )
                                    Text(
                                        text = "Completed '${latestCompleted.serviceTitle}' on ${latestCompleted.scheduledDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF047857)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "How was your experience? Share your rating and feedback to help community members and recognize outstanding specialists.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF065F46)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showReviewSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("leave_completed_service_feedback_button")
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Rate & Leave Feedback for Completed Service", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 4. Customer Testimonials and Ratings Section
            item {
                SectionCard(
                    title = "Customer Testimonials & Verified Reviews",
                    icon = Icons.Default.Star
                ) {
                    val count5 = activeReviews.count { it.rating == 5 }
                    val count4 = activeReviews.count { it.rating == 4 }
                    val count3 = activeReviews.count { it.rating == 3 }
                    val count2 = activeReviews.count { it.rating == 2 }
                    val count1 = activeReviews.count { it.rating == 1 }
                    val totalRevCount = activeReviews.size
                    val calculatedRating = if (activeReviews.isNotEmpty()) {
                        String.format(java.util.Locale.US, "%.1f", activeReviews.map { it.rating }.average())
                    } else {
                        provider.rating.toString()
                    }

                    // Rating Overview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = calculatedRating,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row {
                                repeat(5) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${totalRevCount + provider.reviewCount} total reviews",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        // Star Breakdown Bars
                        Column(modifier = Modifier.weight(1f)) {
                            val p5 = if (totalRevCount > 0) count5.toFloat() / totalRevCount else 0.90f
                            val p4 = if (totalRevCount > 0) count4.toFloat() / totalRevCount else 0.08f
                            val p3 = if (totalRevCount > 0) count3.toFloat() / totalRevCount else 0.02f
                            val p2 = if (totalRevCount > 0) count2.toFloat() / totalRevCount else 0.0f
                            val p1 = if (totalRevCount > 0) count1.toFloat() / totalRevCount else 0.0f

                            RatingBarRow(stars = "5★", percentage = p5, count = "$count5")
                            RatingBarRow(stars = "4★", percentage = p4, count = "$count4")
                            RatingBarRow(stars = "3★", percentage = p3, count = "$count3")
                            RatingBarRow(stars = "2★", percentage = p2, count = "$count2")
                            RatingBarRow(stars = "1★", percentage = p1, count = "$count1")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Action: Leave Feedback Button
                    Button(
                        onClick = { showReviewSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_review_modal_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rate & Leave Feedback for Specialist", fontWeight = FontWeight.Bold)
                    }

                    // Success Message Banner if submitted
                    reviewSubmitSuccessMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFD1FAE5),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(msg, style = MaterialTheme.typography.bodySmall, color = Color(0xFF065F46))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filter Chips & Sort Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reviews (${activeReviews.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Sort toggle
                        TextButton(
                            onClick = { sortMode = if (sortMode == 0) 1 else 0 },
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (sortMode == 0) "Sort: Most Recent" else "Sort: Highest Rating",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Rating Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = ratingFilter == 0,
                            onClick = { ratingFilter = 0 },
                            label = { Text("All (${activeReviews.size})", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = ratingFilter == 5,
                            onClick = { ratingFilter = if (ratingFilter == 5) 0 else 5 },
                            label = { Text("5★ ($count5)", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = ratingFilter == 4,
                            onClick = { ratingFilter = if (ratingFilter == 4) 0 else 4 },
                            label = { Text("4★ ($count4)", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = ratingFilter == 3,
                            onClick = { ratingFilter = if (ratingFilter == 3) 0 else 3 },
                            label = { Text("3★ ($count3)", style = MaterialTheme.typography.labelSmall) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val filteredReviews = activeReviews
                        .filter { if (ratingFilter == 0) true else it.rating == ratingFilter }
                        .let { list ->
                            if (sortMode == 0) list else list.sortedByDescending { it.rating }
                        }

                    if (filteredReviews.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No reviews found for this rating filter.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        // Testimonial Cards
                        filteredReviews.forEach { t ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .testTag("review_card_${t.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = t.authorName.take(1).uppercase(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = t.authorName,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    if (t.isVerified) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = MaterialTheme.colorScheme.tertiaryContainer
                                                        ) {
                                                            Text(
                                                                text = "VERIFIED BOOKING",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        Text(
                                            text = t.dateText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row {
                                            repeat(t.rating) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = Color(0xFFF59E0B),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                            repeat(5 - t.rating) {
                                                Icon(
                                                    imageVector = Icons.Default.StarBorder,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.outlineVariant,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }

                                        if (t.wouldRecommend) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("Recommends Pro", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Color(0xFF059669), fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }

                                    // Praise Tags
                                    if (t.tags.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            t.tags.take(3).forEach { tag ->
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.surface
                                                ) {
                                                    Text(
                                                        text = tag,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontSize = 10.sp,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = t.comment,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 18.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Service: ${t.serviceBooked}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )

                                        // Helpful thumbs button
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier.clickable { onHelpfulReview(t.id) }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.ThumbUp, contentDescription = "Helpful", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Helpful (${t.helpfulCount})", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Interactive Portfolio Case Study Dialog
        selectedPortfolioItem?.let { item ->
            AlertDialog(
                onDismissRequest = { selectedPortfolioItem = null },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("portfolio_case_study_dialog"),
                icon = {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    }
                },
                title = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(item.beforeState, style = MaterialTheme.typography.labelSmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(item.afterState, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Turnaround: ~${item.durationHours}h", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            Text("Cost Range: ${item.costEstimate}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedPortfolioItem = null
                            onBookService(primaryService)
                        }
                    ) {
                        Text("Book Similar Service")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedPortfolioItem = null }) {
                        Text("Close")
                    }
                }
            )
        }

        // Leave Rating & Review Modal Dialog/Sheet
        if (showReviewSheet) {
            LeaveReviewDialog(
                providerName = provider.name,
                servicesOffered = servicesOffered,
                completedBookings = completedBookings,
                onDismiss = { showReviewSheet = false },
                onSubmit = { authorName, rating, comment, serviceBooked, tags, wouldRecommend, bookingId ->
                    onSubmitReview(authorName, rating, comment, serviceBooked, tags, wouldRecommend, bookingId)
                    showReviewSheet = false
                    reviewSubmitSuccessMessage = "Thank you $authorName! Your $rating★ review has been published."
                }
            )
        }
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
            Text(text = title, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun RateBreakdownRow(
    type: String,
    rate: String,
    detail: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(type, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(rate, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
            content()
        }
    }
}

@Composable
fun RatingBarRow(stars: String, percentage: Float, count: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stars, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(22.dp))
        Spacer(modifier = Modifier.width(4.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFFF59E0B),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(count, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveReviewDialog(
    providerName: String,
    servicesOffered: List<ServiceItem>,
    completedBookings: List<BookingEntity>,
    onDismiss: () -> Unit,
    onSubmit: (authorName: String, rating: Int, comment: String, serviceBooked: String, tags: List<String>, wouldRecommend: Boolean, bookingId: Long?) -> Unit
) {
    var selectedService by remember {
        mutableStateOf(completedBookings.firstOrNull()?.serviceTitle ?: servicesOffered.firstOrNull()?.title ?: "On-Demand Service")
    }
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf("Alex Morgan") }
    var wouldRecommend by remember { mutableStateOf(true) }
    var attachedPhotoMock by remember { mutableStateOf(false) }

    val allPraiseTags = listOf(
        "⚡ Punctual & On-Time",
        "🧼 Left Area Spotless",
        "🔧 Expert Craftsmanship",
        "💬 Clear Communication",
        "💰 Fair Upfront Pricing",
        "🛡️ Safety Compliant",
        "⏱️ Fast & Efficient",
        "🤝 Friendly & Polite"
    )
    var selectedTags by remember { mutableStateOf(setOf("⚡ Punctual & On-Time", "🔧 Expert Craftsmanship")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leave_review_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RateReview, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Rate & Review $providerName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Leave feedback after completed service", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Completed Service Selector
                item {
                    Text("Service Completed:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val availableServices = (completedBookings.map { it.serviceTitle } + servicesOffered.map { it.title }).distinct()
                        items(availableServices) { srvTitle ->
                            FilterChip(
                                selected = selectedService == srvTitle,
                                onClick = { selectedService = srvTitle },
                                label = { Text(srvTitle, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                // Interactive 5-Star Selector
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Your Overall Rating:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = { rating = star },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .testTag("star_rating_$star")
                                ) {
                                    Icon(
                                        imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "$star stars",
                                        tint = if (star <= rating) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outlineVariant,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }
                        }

                        val ratingDescription = when (rating) {
                            5 -> "★★★★★ Exceptional - Flawless execution & highest praise"
                            4 -> "★★★★☆ Very Good - Exceeded expectations"
                            3 -> "★★★☆☆ Good - Met standard expectations"
                            2 -> "★★☆☆☆ Fair - Several concerns noted"
                            1 -> "★☆☆☆☆ Poor - Unsatisfactory service"
                            else -> ""
                        }
                        Text(
                            text = ratingDescription,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFD97706)
                        )
                    }
                }

                // Quick-Feedback Praise Tags
                item {
                    Text("Highlights & Praise Tags:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        allPraiseTags.chunked(2).forEach { chunk ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                chunk.forEach { tag ->
                                    val isSelected = selectedTags.contains(tag)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedTags = if (isSelected) selectedTags - tag else selectedTags + tag
                                        },
                                        label = { Text(tag, style = MaterialTheme.typography.labelSmall, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Review Comment Input
                item {
                    Text("Your Review & Feedback:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        placeholder = { Text("Describe the punctuality, cleanliness, quality of results, and attitude...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_comment_input"),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text(
                        text = "${comment.length} / 500 characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }

                // Would Recommend Toggle
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recommend to neighbors?", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = wouldRecommend,
                                onClick = { wouldRecommend = true },
                                label = { Text("👍 Yes") }
                            )
                            FilterChip(
                                selected = !wouldRecommend,
                                onClick = { wouldRecommend = false },
                                label = { Text("👎 No") }
                            )
                        }
                    }
                }

                // Reviewer Name Input
                item {
                    OutlinedTextField(
                        value = authorName,
                        onValueChange = { authorName = it },
                        label = { Text("Your Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Photo Attachment Mockup
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { attachedPhotoMock = !attachedPhotoMock }
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (attachedPhotoMock) Icons.Default.CheckCircle else Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = if (attachedPhotoMock) Color(0xFF059669) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (attachedPhotoMock) "1 Photo Attached: work_result.jpg" else "Attach Completed Job Photo (Optional)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (attachedPhotoMock) FontWeight.Bold else FontWeight.Normal,
                                color = if (attachedPhotoMock) Color(0xFF065F46) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val matchingBookingId = completedBookings.find { it.serviceTitle == selectedService }?.id
                    onSubmit(
                        authorName.ifBlank { "Alex Morgan" },
                        rating,
                        comment.ifBlank { "Specialist delivered high quality service and was very professional." },
                        selectedService,
                        selectedTags.toList(),
                        wouldRecommend,
                        matchingBookingId
                    )
                },
                modifier = Modifier.testTag("submit_review_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Submit Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
