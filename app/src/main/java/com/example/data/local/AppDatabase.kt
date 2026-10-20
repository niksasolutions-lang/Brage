package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BookingEntity::class,
        FavoriteEntity::class,
        SavedAddressEntity::class,
        DownloadedVideo::class,
        Playlist::class,
        PlaylistVideoCrossRef::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun brageDao(): BrageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "brage_services.db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate with default address and an active demo booking
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getInstance(context).brageDao()
                                dao.insertAddress(
                                    SavedAddressEntity(
                                        label = "Home",
                                        street = "742 Evergreen Terrace, Apt 4B",
                                        cityStateZip = "Springfield, OR 97477",
                                        isDefault = true
                                    )
                                )
                                dao.insertAddress(
                                    SavedAddressEntity(
                                        label = "Office",
                                        street = "500 Tech Parkway, Suite 300",
                                        cityStateZip = "Portland, OR 97201",
                                        isDefault = false
                                    )
                                )
                                dao.insertBooking(
                                    BookingEntity(
                                        serviceId = "clean_deep",
                                        serviceTitle = "Whole Home Deep Cleaning",
                                        categoryTitle = "Home Cleaning",
                                        providerName = "Elena Rostova",
                                        providerInitials = "ER",
                                        status = "IN_PROGRESS",
                                        scheduledDate = "Today",
                                        scheduledTimeSlot = "10:00 AM - 1:00 PM",
                                        address = "742 Evergreen Terrace, Apt 4B",
                                        contactPhone = "+1 (555) 382-9912",
                                        basePrice = 129.00,
                                        addOnsDescription = "Eco-friendly Supplies, Inside Oven Steam Clean",
                                        addOnsCost = 35.00,
                                        isExpress = true,
                                        expressFee = 15.00,
                                        totalPrice = 179.00,
                                        specialInstructions = "Please ring the buzzer 4B. The cat will be kept in the guest bedroom.",
                                        userRating = 0,
                                        userReview = ""
                                    )
                                )
                                dao.insertFavorite(FavoriteEntity("repair_plumb"))
                                dao.insertFavorite(FavoriteEntity("tech_smart_wifi"))
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
