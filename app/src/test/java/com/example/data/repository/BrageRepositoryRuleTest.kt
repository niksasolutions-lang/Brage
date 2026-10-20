package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.local.BookingEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test
import kotlin.test.assertFailsWith

class BrageRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createBooking_authenticatedUser_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = BrageFirestoreRepository(firestore)

        val sampleBooking = BookingEntity(
            serviceId = "clean_deep",
            serviceTitle = "Whole Home Deep Cleaning",
            categoryTitle = "Home Cleaning",
            providerName = "Elena Rostova",
            providerInitials = "ER",
            status = "CONFIRMED",
            scheduledDate = "Tomorrow, Oct 10",
            scheduledTimeSlot = "10:00 AM - 1:00 PM",
            address = "742 Evergreen Terrace",
            contactPhone = "+1 (555) 382-9912",
            basePrice = 129.0,
            addOnsDescription = "Eco Supplies",
            addOnsCost = 15.0,
            isExpress = false,
            expressFee = 0.0,
            totalPrice = 144.0,
            specialInstructions = "Ring buzzer 4B",
            userRating = 0,
            userReview = ""
        )

        val bookingId = withTimeout(5000L) {
            repository.createBooking(sampleBooking)
        }
        assertTrue(bookingId.isNotEmpty())

        val bookings = withTimeout(5000L) {
            repository.observeBookings().first { it.isNotEmpty() }
        }
        assertTrue(bookings.any { it.serviceId == "clean_deep" })
    }

    @Test
    fun toggleFavorite_authenticatedUser_savesAndDeletes() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = BrageFirestoreRepository(firestore)

        withTimeout(5000L) {
            repository.toggleFavorite("repair_plumb", false)
        }

        val favs = withTimeout(5000L) {
            repository.observeFavorites().first { it.isNotEmpty() }
        }
        assertTrue(favs.contains("repair_plumb"))

        withTimeout(5000L) {
            repository.toggleFavorite("repair_plumb", true)
        }

        val favsAfter = withTimeout(5000L) {
            repository.observeFavorites().first { it.isEmpty() }
        }
        assertFalse(favsAfter.contains("repair_plumb"))
    }

    @Test
    fun unauthenticatedAccess_throwsIllegalStateException() = runBlocking {
        auth.signOut()
        val repository = BrageFirestoreRepository(firestore)

        assertFailsWith<IllegalStateException> {
            repository.requireUserId()
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@bragetest.com"
    }
}

