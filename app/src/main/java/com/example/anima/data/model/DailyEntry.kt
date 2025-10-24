package com.example.anima.data.model

@androidx.room.Entity(
    tableName = "daily_entries",
    indices = [androidx.room.Index(value = ["date"], unique = true)]
)
data class DailyEntry(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    // store LocalDate as epochDay
    val date: Long,
    val mood: Int? = null,
    val note: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
