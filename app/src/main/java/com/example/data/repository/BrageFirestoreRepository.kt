package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.BookingEntity
import com.example.data.local.SavedAddressEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class BrageFirestoreRepository(val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- Profile ---
    suspend fun saveUserProfile(displayName: String, email: String, photoUrl: String? = null) {
        val uid = requireUserId()
        val userRef = db.collection("users").document(uid)
        val payload = mutableMapOf<String, Any>(
            "userId" to uid,
            "displayName" to displayName,
            "email" to email,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!photoUrl.isNullOrBlank()) {
            payload["photoUrl"] = photoUrl
        }
        userRef.set(payload).await()
    }

    // --- Bookings ---
    fun observeBookings(): Flow<List<BookingEntity>> = flow {
        val uid = requireUserId()
        val bookingsRef = db.collection("users").document(uid).collection("bookings")
        emitAll(
            bookingsRef.snapshots().map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        BookingEntity(
                            id = (doc.getString("id")?.hashCode()?.toLong() ?: 0L),
                            serviceId = doc.getString("serviceId") ?: "",
                            serviceTitle = doc.getString("serviceTitle") ?: "",
                            categoryTitle = doc.getString("categoryTitle") ?: "",
                            providerName = doc.getString("providerName") ?: "",
                            providerInitials = doc.getString("providerInitials") ?: "",
                            status = doc.getString("status") ?: "CONFIRMED",
                            scheduledDate = doc.getString("scheduledDate") ?: "",
                            scheduledTimeSlot = doc.getString("scheduledTimeSlot") ?: "",
                            address = doc.getString("address") ?: "",
                            contactPhone = doc.getString("contactPhone") ?: "",
                            basePrice = doc.getDouble("basePrice") ?: 0.0,
                            addOnsDescription = doc.getString("addOnsDescription") ?: "",
                            addOnsCost = doc.getDouble("addOnsCost") ?: 0.0,
                            isExpress = doc.getBoolean("isExpress") ?: false,
                            expressFee = doc.getDouble("expressFee") ?: 0.0,
                            totalPrice = doc.getDouble("totalPrice") ?: 0.0,
                            specialInstructions = doc.getString("specialInstructions") ?: "",
                            userRating = (doc.getLong("userRating") ?: 0L).toInt(),
                            userReview = doc.getString("userReview") ?: "",
                            createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        Log.e("BrageFirestore", "Error parsing booking", e)
                        null
                    }
                }
            }.catch { err ->
                Log.e("BrageFirestore", "Error observing bookings", err)
                emit(emptyList())
            }
        )
    }

    suspend fun createBooking(booking: BookingEntity): String {
        val uid = requireUserId()
        val bookingId = "book_" + UUID.randomUUID().toString().replace("-", "").take(16)
        val docRef = db.collection("users").document(uid).collection("bookings").document(bookingId)

        val payload = mapOf(
            "id" to bookingId,
            "userId" to uid,
            "serviceId" to booking.serviceId,
            "serviceTitle" to booking.serviceTitle,
            "categoryTitle" to booking.categoryTitle,
            "providerName" to booking.providerName,
            "providerInitials" to booking.providerInitials,
            "status" to booking.status,
            "scheduledDate" to booking.scheduledDate,
            "scheduledTimeSlot" to booking.scheduledTimeSlot,
            "address" to booking.address,
            "contactPhone" to booking.contactPhone,
            "basePrice" to booking.basePrice,
            "addOnsDescription" to booking.addOnsDescription,
            "addOnsCost" to booking.addOnsCost,
            "isExpress" to booking.isExpress,
            "expressFee" to booking.expressFee,
            "totalPrice" to booking.totalPrice,
            "specialInstructions" to booking.specialInstructions,
            "userRating" to booking.userRating,
            "userReview" to booking.userReview,
            "createdAt" to FieldValue.serverTimestamp()
        )

        docRef.set(payload).await()
        return bookingId
    }

    suspend fun updateBookingStatus(bookingId: String, status: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("bookings").document(bookingId)
        docRef.update(
            mapOf(
                "status" to status,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun updateBookingReview(bookingId: String, rating: Int, review: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("bookings").document(bookingId)
        docRef.update(
            mapOf(
                "userRating" to rating,
                "userReview" to review,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun rescheduleBooking(bookingId: String, newDate: String, newTime: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("bookings").document(bookingId)
        docRef.update(
            mapOf(
                "scheduledDate" to newDate,
                "scheduledTimeSlot" to newTime,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun cancelBooking(bookingId: String) {
        updateBookingStatus(bookingId, "CANCELLED")
    }

    // --- Favorites ---
    fun observeFavorites(): Flow<List<String>> = flow {
        val uid = requireUserId()
        val favRef = db.collection("users").document(uid).collection("favorites")
        emitAll(
            favRef.snapshots().map { snapshot ->
                snapshot.documents.mapNotNull { it.getString("serviceId") }
            }.catch {
                emit(emptyList())
            }
        )
    }

    suspend fun toggleFavorite(serviceId: String, currentIsFav: Boolean) {
        val uid = requireUserId()
        val favDoc = db.collection("users").document(uid).collection("favorites").document(serviceId)
        if (currentIsFav) {
            favDoc.delete().await()
        } else {
            val payload = mapOf(
                "serviceId" to serviceId,
                "createdAt" to FieldValue.serverTimestamp()
            )
            favDoc.set(payload).await()
        }
    }

    // --- Saved Addresses ---
    fun observeAddresses(): Flow<List<SavedAddressEntity>> = flow {
        val uid = requireUserId()
        val addrRef = db.collection("users").document(uid).collection("addresses")
        emitAll(
            addrRef.snapshots().map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    SavedAddressEntity(
                        id = doc.id.hashCode().toLong(),
                        label = doc.getString("label") ?: "Address",
                        street = doc.getString("street") ?: "",
                        cityStateZip = doc.getString("cityStateZip") ?: "",
                        isDefault = doc.getBoolean("isDefault") ?: false
                    )
                }
            }.catch {
                emit(emptyList())
            }
        )
    }

    suspend fun saveAddress(address: SavedAddressEntity): String {
        val uid = requireUserId()
        val addrId = "addr_" + UUID.randomUUID().toString().replace("-", "").take(12)
        val docRef = db.collection("users").document(uid).collection("addresses").document(addrId)
        val payload = mapOf(
            "id" to addrId,
            "label" to address.label,
            "street" to address.street,
            "cityStateZip" to address.cityStateZip,
            "isDefault" to address.isDefault,
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
        return addrId
    }

    suspend fun deleteAddress(addressId: String) {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("addresses").document(addressId).delete().await()
    }
}
