package com.example.anima.data

import com.example.anima.data.model.DailyEntry

class DailyRepository(private val dao: DailyEntryDao) {

    suspend fun getByDate(epochDay: Long): DailyEntry? = dao.getByDate(epochDay)

    suspend fun insert(entry: DailyEntry): Long = dao.insert(entry)

    suspend fun update(entry: DailyEntry) = dao.update(entry)

    suspend fun getBetween(start: Long, end: Long): List<DailyEntry> = dao.getBetween(start, end)

    suspend fun getAll(): List<DailyEntry> = dao.getAll()
}

