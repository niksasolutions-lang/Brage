package com.example.data.repository

import com.example.data.local.BookingEntity
import com.example.data.local.BrageDao
import com.example.data.local.DownloadedVideo
import com.example.data.local.FavoriteEntity
import com.example.data.local.Playlist
import com.example.data.local.PlaylistVideoCrossRef
import com.example.data.local.SavedAddressEntity
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BrageRepository(private val dao: BrageDao) : VideoRepository {

    // --- In-Memory Catalog spanning ALL Categories ---

    val categories: List<ServiceCategory> = listOf(
        ServiceCategory(
            id = "cat_cleaning",
            title = "Home Cleaning",
            subtitle = "Deep sanitization, move-out & regular maid visits",
            iconName = "CleaningServices",
            accentColorHex = 0xFF2563EB, // Royal Blue
            popularTag = "Most Popular",
            availableCount = 18
        ),
        ServiceCategory(
            id = "cat_repairs",
            title = "Repairs & Handyman",
            subtitle = "Plumbing, electrical, furniture & installations",
            iconName = "Build",
            accentColorHex = 0xFFD97706, // Amber
            popularTag = "Fast Response",
            availableCount = 24
        ),
        ServiceCategory(
            id = "cat_beauty",
            title = "Beauty & Grooming",
            subtitle = "Haircut, barbering, manicure & massage at home",
            iconName = "Spa",
            accentColorHex = 0xFFDB2777, // Pink/Rose
            popularTag = null,
            availableCount = 15
        ),
        ServiceCategory(
            id = "cat_wellness",
            title = "Wellness & Fitness",
            subtitle = "1-on-1 personal training, yoga & sports therapy",
            iconName = "FitnessCenter",
            accentColorHex = 0xFF059669, // Emerald
            popularTag = null,
            availableCount = 12
        ),
        ServiceCategory(
            id = "cat_moving",
            title = "Moving & Delivery",
            subtitle = "Apartment moving, heavy furniture & express cargo",
            iconName = "LocalShipping",
            accentColorHex = 0xFF7C3AED, // Violet
            popularTag = "Same-Day",
            availableCount = 14
        ),
        ServiceCategory(
            id = "cat_tech",
            title = "Tech & Smart Home",
            subtitle = "Mesh Wi-Fi setup, TV mounting, PC & smart locks",
            iconName = "Devices",
            accentColorHex = 0xFF0284C7, // Sky Blue
            popularTag = "Certified Pros",
            availableCount = 16
        ),
        ServiceCategory(
            id = "cat_pets",
            title = "Pet Care & Sitting",
            subtitle = "Mobile dog grooming, dog walking & vet checkups",
            iconName = "Pets",
            accentColorHex = 0xFFEA580C, // Orange
            popularTag = null,
            availableCount = 11
        ),
        ServiceCategory(
            id = "cat_auto",
            title = "Auto & Detailing",
            subtitle = "Mobile car wash, interior steam & oil servicing",
            iconName = "DirectionsCar",
            accentColorHex = 0xFF475569, // Slate
            popularTag = "At Your Door",
            availableCount = 10
        ),
        ServiceCategory(
            id = "cat_tutoring",
            title = "Tutoring & Lessons",
            subtitle = "STEM coaching, guitar, language & exam prep",
            iconName = "School",
            accentColorHex = 0xFF4F46E5, // Indigo
            popularTag = null,
            availableCount = 13
        ),
        ServiceCategory(
            id = "cat_events",
            title = "Events & Culinary",
            subtitle = "Private chefs, event photography & party sound",
            iconName = "Celebration",
            accentColorHex = 0xFFBE123C, // Crimson
            popularTag = "Top Tier",
            availableCount = 9
        )
    )

    val allServices: List<ServiceItem> = listOf(
        // --- 1. CLEANING ---
        ServiceItem(
            id = "clean_deep",
            categoryId = "cat_cleaning",
            title = "Whole Home Deep Cleaning",
            subtitle = "Detailed floor-to-ceiling sanitization including baseboards and kitchen",
            basePrice = 129.0,
            priceUnit = "/ visit",
            rating = 4.94,
            reviewCount = 214,
            durationMinutes = 180,
            description = "Comprehensive multi-room deep cleaning service by our top-rated certified housekeeping team. Covers living rooms, master bedroom, full bath disinfection, kitchen degreasing, and detailed dusting.",
            includedHighlights = listOf(
                "Hospital-grade disinfectant & HEPA filtration vacuuming",
                "Appliance exteriors & countertop deep sanitize",
                "Tile scrub & bathroom mold prevention treatment",
                "Full trash disposal and fresh bed sheet change"
            ),
            provider = ProviderInfo(
                id = "p_elena",
                name = "Elena Rostova",
                title = "Master Housekeeper & Sanitation Specialist",
                rating = 4.96,
                reviewCount = 380,
                completedJobs = 620,
                experienceYears = 8,
                badge = "Top Pro Elite",
                initials = "ER",
                bio = "Passionate about allergen-free, immaculate homes. 8+ years serving city households with 100% satisfaction guarantee."
            ),
            instantBookable = true,
            expressAvailable = true,
            addOns = listOf(
                ServiceAddOn("addon_clean_1", "Eco-friendly Plant-based Supplies", 15.0, 0),
                ServiceAddOn("addon_clean_2", "Inside Oven Steam Clean", 25.0, 30),
                ServiceAddOn("addon_clean_3", "Interior Refrigerator Scrub", 20.0, 20),
                ServiceAddOn("addon_clean_4", "Window Interiors & Tracks (up to 8)", 35.0, 40)
            )
        ),
        ServiceItem(
            id = "clean_move",
            categoryId = "cat_cleaning",
            title = "Move-In / Move-Out Clean",
            subtitle = "Guarantee deposit refund standard with empty home deep scrubbing",
            basePrice = 179.0,
            priceUnit = "/ visit",
            rating = 4.88,
            reviewCount = 142,
            durationMinutes = 240,
            description = "Thorough checklist clean designed for lease transitions and property turnovers. Ensures every drawer, closet, cabinet, and floorboard passes property management scrutiny.",
            includedHighlights = listOf(
                "Inside and outside of all cabinets & drawers",
                "Deep oven, stove, fridge, and dishwasher cleanse",
                "Baseboards, door frames, switch plates & fixtures",
                "Pre-inspection checklist signed off"
            ),
            provider = ProviderInfo(
                id = "p_marcus",
                name = "Marcus Vance",
                title = "Lease Handover Cleaning Lead",
                rating = 4.91,
                reviewCount = 195,
                completedJobs = 310,
                experienceYears = 6,
                badge = "Deposit Safe",
                initials = "MV",
                bio = "Specializing in tenant move-outs. 99% deposit return rate across 300+ properties."
            ),
            addOns = listOf(
                ServiceAddOn("addon_move_1", "Carpet Stain Deep Extraction", 45.0, 45),
                ServiceAddOn("addon_move_2", "Balcony / Patio Power Wash", 35.0, 30)
            )
        ),

        // --- 2. REPAIRS ---
        ServiceItem(
            id = "repair_plumb",
            categoryId = "cat_repairs",
            title = "Plumbing Leak & Drain Unclog",
            subtitle = "Fast fix for dripping pipes, clogged sinks, toilets and disposal units",
            basePrice = 85.0,
            priceUnit = "/ hr",
            rating = 4.97,
            reviewCount = 310,
            durationMinutes = 60,
            description = "Licensed master plumbers equipped with diagnostic cameras, power snakes, and pipe replacement fittings to resolve clogs and leaks swiftly without water damage.",
            includedHighlights = listOf(
                "Full diagnostics with non-invasive pipe scoping",
                "P-trap and main line clean-out clearing",
                "Fixture valve gasket and washer replacement",
                "90-day leak-free guarantee"
            ),
            provider = ProviderInfo(
                id = "p_david",
                name = "David Chen",
                title = "Licensed Journeyman Plumber #PL-8924",
                rating = 4.98,
                reviewCount = 420,
                completedJobs = 850,
                experienceYears = 12,
                badge = "Licensed Pro",
                initials = "DC",
                bio = "12 years commercial & residential plumbing. Prompt, clean, and respectful of your home."
            ),
            instantBookable = true,
            expressAvailable = true,
            addOns = listOf(
                ServiceAddOn("addon_plumb_1", "High-pressure Hydro Drain Jetting", 40.0, 30),
                ServiceAddOn("addon_plumb_2", "New Garbage Disposal Install", 55.0, 45)
            )
        ),
        ServiceItem(
            id = "repair_furniture",
            categoryId = "cat_repairs",
            title = "Furniture Assembly & Mounting",
            subtitle = "Expert assembly for beds, desks, IKEA, wardrobes and wall shelving",
            basePrice = 65.0,
            priceUnit = "/ hr",
            rating = 4.92,
            reviewCount = 188,
            durationMinutes = 90,
            description = "Stop struggling with complicated instruction manuals. Our handyman brings heavy duty power drivers, laser levels, and wall anchors for rock-solid stability.",
            includedHighlights = listOf(
                "Precision assembly and hardware torque checking",
                "Heavy anti-tip safety wall anchoring included",
                "Cardboard flattening and packaging cleanup",
                "Hardware double-checked for zero wobble"
            ),
            provider = ProviderInfo(
                id = "p_sarah",
                name = "Sarah Jenkins",
                title = "Master Assembler & Handyman",
                rating = 4.95,
                reviewCount = 280,
                completedJobs = 540,
                experienceYears = 7,
                badge = "5-Star Assembler",
                initials = "SJ",
                bio = "Assembled 1,000+ pieces from IKEA, West Elm, CB2, and Wayfair. Rapid and laser-precise."
            ),
            addOns = listOf(
                ServiceAddOn("addon_furn_1", "Heavy Mirror / Art Hanging (up to 3)", 25.0, 20),
                ServiceAddOn("addon_furn_2", "Disassembly of Old Unit", 30.0, 30)
            )
        ),

        // --- 3. BEAUTY ---
        ServiceItem(
            id = "beauty_hair",
            categoryId = "cat_beauty",
            title = "In-Home Haircut & Blowout Styling",
            subtitle = "Salon-grade haircut, blow dry, and custom styling in your living room",
            basePrice = 75.0,
            priceUnit = "/ session",
            rating = 4.95,
            reviewCount = 165,
            durationMinutes = 60,
            description = "Get a high-end salon experience right at home. Includes custom consultation, dry or wet precision cutting, luxurious texturizing, blowout styling, and cleanup.",
            includedHighlights = listOf(
                "Consultation tailored to face shape & lifestyle",
                "Professional styling products included (Oribe / Davines)",
                "Sanitized professional shears and hot tools",
                "Floor protection mat and post-cut sweep"
            ),
            provider = ProviderInfo(
                id = "p_chloe",
                name = "Chloe Dubois",
                title = "Editorial Hairstylist & Colorist",
                rating = 4.97,
                reviewCount = 210,
                completedJobs = 490,
                experienceYears = 9,
                badge = "Celebrity Stylist",
                initials = "CD",
                bio = "Former fashion week stylist bringing runway precision and relaxed salon vibes right to your home."
            ),
            addOns = listOf(
                ServiceAddOn("addon_hair_1", "Deep Conditioning Keratin Treatment", 30.0, 20),
                ServiceAddOn("addon_hair_2", "Scalp Detox & Massage", 20.0, 15)
            )
        ),
        ServiceItem(
            id = "beauty_massage",
            categoryId = "cat_beauty",
            title = "Deep Tissue & Swedish Mobile Massage",
            subtitle = "Licensed massage therapist arrives with heated table and organic oils",
            basePrice = 110.0,
            priceUnit = "/ 60 min",
            rating = 4.98,
            reviewCount = 230,
            durationMinutes = 60,
            description = "Relax deeply without having to drive anywhere afterward. Therapist brings portable massage table, fresh luxury linens, aromatherapy diffuser, and heated oils.",
            includedHighlights = listOf(
                "Customized pressure from gentle relaxation to deep knot release",
                "Choice of organic jojoba or lavender massage oil",
                "Heated table pad and soothing soundscape",
                "Post-session posture guidance"
            ),
            provider = ProviderInfo(
                id = "p_kenji",
                name = "Kenji Takahashi, LMT",
                title = "Certified Sports & Neuromuscular Therapist",
                rating = 4.99,
                reviewCount = 340,
                completedJobs = 720,
                experienceYears = 11,
                badge = "Board Certified",
                initials = "KT",
                bio = "Specializing in chronic tension relief, neck stiffness, and recovery for active professionals."
            ),
            addOns = listOf(
                ServiceAddOn("addon_msg_1", "Extend to 90 Minutes", 45.0, 30),
                ServiceAddOn("addon_msg_2", "Hot Basalt Stone Therapy", 25.0, 15)
            )
        ),

        // --- 4. WELLNESS ---
        ServiceItem(
            id = "well_personal_train",
            categoryId = "cat_wellness",
            title = "1-on-1 Fitness Training & Coaching",
            subtitle = "Custom workout in your apartment, building gym, or local park",
            basePrice = 70.0,
            priceUnit = "/ session",
            rating = 4.91,
            reviewCount = 98,
            durationMinutes = 60,
            description = "Certified trainer brings resistance bands, kettlebells, and personalized programming tailored to your goals: fat loss, muscle tone, strength, or mobility.",
            includedHighlights = listOf(
                "Initial body composition & movement assessment",
                "High-intensity interval or hypertrophy circuit",
                "Corrective form coaching to prevent injury",
                "Digital weekly training plan in Brage app"
            ),
            provider = ProviderInfo(
                id = "p_malik",
                name = "Malik Thorne, CSCS",
                title = "Strength & Performance Coach",
                rating = 4.93,
                reviewCount = 145,
                completedJobs = 410,
                experienceYears = 8,
                badge = "CSCS Certified",
                initials = "MT",
                bio = "Dedicated to sustainable fitness habits that build real strength without burnout."
            ),
            addOns = listOf(
                ServiceAddOn("addon_train_1", "Nutritional Macro Meal Plan", 35.0, 0),
                ServiceAddOn("addon_train_2", "Add Workout Partner (2-on-1)", 30.0, 0)
            )
        ),

        // --- 5. MOVING ---
        ServiceItem(
            id = "move_local_apt",
            categoryId = "cat_moving",
            title = "Local Apartment Moving (2 Pros + Van)",
            subtitle = "Stress-free local move with blankets, dollies, straps and protected hauling",
            basePrice = 115.0,
            priceUnit = "/ hr",
            rating = 4.89,
            reviewCount = 175,
            durationMinutes = 120,
            description = "Professional moving duo with padded moving truck, shrink wrap, and floor runners. Careful loading, secure transport, and room-by-room placement.",
            includedHighlights = listOf(
                "Full transit cargo protection insurance included",
                "Padded furniture blankets and stretch film protection",
                "Door jamb and corner guard protectors",
                "Free wardrobe box usage for clothes on hangers"
            ),
            provider = ProviderInfo(
                id = "p_anton",
                name = "Anton & Team (SwiftMove)",
                title = "Licensed Urban Moving Specialist",
                rating = 4.90,
                reviewCount = 260,
                completedJobs = 680,
                experienceYears = 10,
                badge = "Fully Insured",
                initials = "SM",
                bio = "Fast, careful urban movers who treat your heirlooms and television like fragile glass."
            ),
            instantBookable = true,
            addOns = listOf(
                ServiceAddOn("addon_move_3", "Add 3rd Pro for Faster Move", 45.0, 0),
                ServiceAddOn("addon_move_4", "Pre-Move Box Packing (10 boxes)", 60.0, 60)
            )
        ),

        // --- 6. TECH ---
        ServiceItem(
            id = "tech_smart_wifi",
            categoryId = "cat_tech",
            title = "Smart Home & Mesh Wi-Fi Setup",
            subtitle = "Eliminate Wi-Fi dead zones, install smart thermostats, cameras & locks",
            basePrice = 95.0,
            priceUnit = "/ setup",
            rating = 4.96,
            reviewCount = 189,
            durationMinutes = 75,
            description = "Certified tech technician optimizes your home network, configures mesh routers (Eero, Nest, Asus), pairs smart doorbells, and connects Alexa / Google Home ecosystems.",
            includedHighlights = listOf(
                "Network speed test & optimal node placement",
                "Secure WPA3 encryption & guest network configuration",
                "App walkthrough on your phone and tablet",
                "30-day remote tech support included"
            ),
            provider = ProviderInfo(
                id = "p_alex",
                name = "Alex Rivera",
                title = "Senior IoT & Network Engineer",
                rating = 4.98,
                reviewCount = 310,
                completedJobs = 790,
                experienceYears = 9,
                badge = "CompTIA Network+",
                initials = "AR",
                bio = "Solved 700+ slow internet setups. I make smart homes seamless and frustration-free."
            ),
            instantBookable = true,
            addOns = listOf(
                ServiceAddOn("addon_tech_1", "TV Wall Mount (up to 65 inch)", 50.0, 45),
                ServiceAddOn("addon_tech_2", "Smart Doorbell Install & Wiring", 35.0, 30)
            )
        ),

        // --- 7. PETS ---
        ServiceItem(
            id = "pet_grooming_mobile",
            categoryId = "cat_pets",
            title = "Full Mobile Dog Grooming & Spa",
            subtitle = "Self-contained van arrives at your curb: bath, haircut, nail trim & teeth brushing",
            basePrice = 85.0,
            priceUnit = "/ pet",
            rating = 4.93,
            reviewCount = 156,
            durationMinutes = 60,
            description = "No stressful cages or crowded salon waits. Our climate-controlled luxury grooming van parks right outside. Hypoallergenic blueberry facial, warm hydro-bath, fluff dry, and paw balm.",
            includedHighlights = listOf(
                "Warm hydro-massage bath with natural organic shampoo",
                "Breed-specific styling or sanitary trim",
                "Gentle nail clipping and dremel grind",
                "Ear cleaning, gland expression & paw butter"
            ),
            provider = ProviderInfo(
                id = "p_hannah",
                name = "Hannah Brooks",
                title = "Certified Master Pet Stylist",
                rating = 4.96,
                reviewCount = 240,
                completedJobs = 610,
                experienceYears = 7,
                badge = "Fear-Free Certified",
                initials = "HB",
                bio = "Trained in low-stress handling. Your pup gets 100% gentle, undivided one-on-one attention."
            ),
            addOns = listOf(
                ServiceAddOn("addon_pet_1", "Flea & Tick Prevention Rinse", 18.0, 15),
                ServiceAddOn("addon_pet_2", "De-shedding Furminator Treatment", 25.0, 20)
            )
        ),

        // --- 8. AUTO ---
        ServiceItem(
            id = "auto_detail_mobile",
            categoryId = "cat_auto",
            title = "Mobile Auto Detail & Clay Wax",
            subtitle = "Showroom gloss at your driveway: foam wash, interior steam & ceramic wax",
            basePrice = 99.0,
            priceUnit = "/ vehicle",
            rating = 4.94,
            reviewCount = 167,
            durationMinutes = 90,
            description = "Professional mobile detailers arrive equipped with filtered spot-free water tank, generator, foam cannon, and commercial steam extractor.",
            includedHighlights = listOf(
                "Hand foam cannon wash & microfiber towel dry",
                "Deep interior vacuum, dash UV protectant & cup holders",
                "Tire shine, rim decontamination & glass buff",
                "Protective carnauba polymer spray wax"
            ),
            provider = ProviderInfo(
                id = "p_diego",
                name = "Diego Morales",
                title = "IDA Certified Auto Detailer",
                rating = 4.97,
                reviewCount = 290,
                completedJobs = 730,
                experienceYears = 8,
                badge = "IDA Certified",
                initials = "DM",
                bio = "Obsessed with perfect paint reflection and spotless interiors. We bring our own water & power."
            ),
            instantBookable = true,
            expressAvailable = true,
            addOns = listOf(
                ServiceAddOn("addon_auto_1", "Leather Conditioning & Stain Shield", 28.0, 20),
                ServiceAddOn("addon_auto_2", "Headlight Cloudiness Restoration", 35.0, 30)
            )
        ),

        // --- 9. TUTORING ---
        ServiceItem(
            id = "tutor_math_stem",
            categoryId = "cat_tutoring",
            title = "STEM & Mathematics Private Coaching",
            subtitle = "Algebra, Calculus, Physics and SAT/ACT prep by university honors grads",
            basePrice = 55.0,
            priceUnit = "/ hr",
            rating = 4.92,
            reviewCount = 88,
            durationMinutes = 60,
            description = "Transform math anxiety into confidence. Patient, step-by-step problem breakdown with tailored practice problem sets and test-taking strategies.",
            includedHighlights = listOf(
                "Curriculum alignment with your student's school syllabus",
                "Homework review and exam prep drill",
                "Visual intuition tricks for complex formulas",
                "Session progress summary sent to parents"
            ),
            provider = ProviderInfo(
                id = "p_priya",
                name = "Priya Sharma, M.S.",
                title = "Senior Math & Physics Educator",
                rating = 4.95,
                reviewCount = 130,
                completedJobs = 340,
                experienceYears = 6,
                badge = "Master Educator",
                initials = "PS",
                bio = "Former university teaching assistant helping students jump 2 letter grades on average."
            ),
            addOns = listOf(
                ServiceAddOn("addon_tutor_1", "Weekly Practice Exam Pack with Solutions", 20.0, 0),
                ServiceAddOn("addon_tutor_2", "College Admissions Essay Review", 40.0, 45)
            )
        ),

        // --- 10. EVENTS ---
        ServiceItem(
            id = "event_private_chef",
            categoryId = "cat_events",
            title = "Private Chef Multi-Course Dinner",
            subtitle = "Restaurant-caliber dining in your kitchen with grocery shopping & dishwashing",
            basePrice = 180.0,
            priceUnit = "/ dinner",
            rating = 4.98,
            reviewCount = 112,
            durationMinutes = 180,
            description = "Celebrate birthdays, anniversaries, or dinner parties effortlessly. Chef sources prime local ingredients, prepares an exquisite 3-course menu, serves guests, and leaves kitchen spotless.",
            includedHighlights = listOf(
                "Custom menu design with dietary substitutions",
                "Farm-to-table artisan ingredients sourced morning of event",
                "Plated tableside service with wine pairing recommendations",
                "Full kitchen cleanup and shining dish wash"
            ),
            provider = ProviderInfo(
                id = "p_giovanni",
                name = "Chef Giovanni Rossi",
                title = "Michelin Alum & Private Caterer",
                rating = 4.99,
                reviewCount = 160,
                completedJobs = 290,
                experienceYears = 14,
                badge = "Michelin Trained",
                initials = "GR",
                bio = "14 years in fine dining across Milan and New York. Creating unforgettable intimate dining moments."
            ),
            addOns = listOf(
                ServiceAddOn("addon_chef_1", "Sommelier Wine Pairing Selection", 50.0, 0),
                ServiceAddOn("addon_chef_2", "Custom Artisan Dessert & Cake", 40.0, 30)
            )
        )
    )

    // Helper queries
    fun getCategoryById(id: String): ServiceCategory? {
        return categories.find { it.id == id }
    }

    fun getServiceById(id: String): ServiceItem? {
        return allServices.find { it.id == id }
    }

    fun getServicesByCategory(categoryId: String): List<ServiceItem> {
        return allServices.filter { it.categoryId == categoryId }
    }

    // Room Bookings
    val allBookings: Flow<List<BookingEntity>> = dao.getAllBookings()

    fun getBookingById(id: Long): Flow<BookingEntity?> = dao.getBookingById(id)

    suspend fun createBooking(booking: BookingEntity): Long = dao.insertBooking(booking)

    suspend fun updateBookingStatus(id: Long, status: String) = dao.updateBookingStatus(id, status)

    suspend fun submitReview(id: Long, rating: Int, review: String) = dao.updateBookingReview(id, rating, review)

    suspend fun reschedule(id: Long, newDate: String, newTime: String) = dao.rescheduleBooking(id, newDate, newTime)

    suspend fun cancelBooking(id: Long) = dao.updateBookingStatus(id, "CANCELLED")

    // Room Favorites
    val allFavorites: Flow<List<FavoriteEntity>> = dao.getAllFavorites()

    fun isFavorite(serviceId: String): Flow<Boolean> = dao.isFavorite(serviceId).map { it > 0 }

    suspend fun toggleFavorite(serviceId: String, currentIsFav: Boolean) {
        if (currentIsFav) {
            dao.deleteFavorite(serviceId)
        } else {
            dao.insertFavorite(FavoriteEntity(serviceId))
        }
    }

    // Addresses
    val allAddresses: Flow<List<SavedAddressEntity>> = dao.getAllAddresses()

    suspend fun addAddress(address: SavedAddressEntity): Long = dao.insertAddress(address)

    suspend fun removeAddress(id: Long) = dao.deleteAddress(id)

    // Downloaded Videos
    override fun getAllDownloadedVideos(): Flow<List<DownloadedVideo>> = dao.getAllDownloadedVideos()
    override suspend fun insertDownloadedVideo(video: DownloadedVideo) = dao.insertDownloadedVideo(video)
    override suspend fun deleteDownloadedVideo(videoId: String) = dao.deleteDownloadedVideo(videoId)

    // Playlists
    override fun getAllPlaylists(): Flow<List<Playlist>> = dao.getAllPlaylists()
    override suspend fun createPlaylist(name: String): Long = dao.insertPlaylist(Playlist(name = name))
    override suspend fun deletePlaylist(playlistId: Long) = dao.deletePlaylist(playlistId)
    override suspend fun addVideoToPlaylist(playlistId: Long, videoId: String) = dao.insertPlaylistVideoCrossRef(PlaylistVideoCrossRef(playlistId, videoId))
    override suspend fun removeVideoFromPlaylist(playlistId: Long, videoId: String) = dao.deletePlaylistVideo(playlistId, videoId)
    override fun getVideosInPlaylist(playlistId: Long): Flow<List<DownloadedVideo>> = dao.getVideosInPlaylist(playlistId)
}
