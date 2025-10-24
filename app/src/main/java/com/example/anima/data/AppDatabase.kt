package com.example.anima.data

import android.content.Context
import com.example.anima.data.model.DailyEntry

@androidx.room.Database(entities = [DailyEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : androidx.room.RoomDatabase() {
    abstract fun dailyEntryDao(): DailyEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anima_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
