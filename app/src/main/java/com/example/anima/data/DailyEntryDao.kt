package com.example.anima.data

import com.example.anima.data.model.DailyEntry

@androidx.room.Dao
interface DailyEntryDao {
    @androidx.room.Query("SELECT * FROM daily_entries WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: Long): DailyEntry?

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DailyEntry): Long

    @androidx.room.Update
    suspend fun update(entry: DailyEntry)

    @androidx.room.Query("SELECT * FROM daily_entries WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    suspend fun getBetween(start: Long, end: Long): List<DailyEntry>

    @androidx.room.Query("SELECT * FROM daily_entries ORDER BY date ASC")
    suspend fun getAll(): List<DailyEntry>
}
