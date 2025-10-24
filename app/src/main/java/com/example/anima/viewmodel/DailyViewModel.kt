package com.example.anima.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.anima.data.AppDatabase
import com.example.anima.data.DailyRepository
import com.example.anima.data.model.DailyEntry
import com.example.anima.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DailyViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).dailyEntryDao()
    private val repo = DailyRepository(dao)

    private val _currentEntry = MutableStateFlow<DailyEntry?>(null)
    val currentEntry: StateFlow<DailyEntry?> = _currentEntry

    init {
        ensureTodayEntry()
    }

    fun ensureTodayEntry() {
        viewModelScope.launch {
            val today = DateUtils.todayEpochDay()
            val existing = repo.getByDate(today)
            if (existing == null) {
                val entry = DailyEntry(date = today)
                val id = repo.insert(entry)
                _currentEntry.value = entry.copy(id = id)
            } else {
                _currentEntry.value = existing
            }
        }
    }

    fun updateMoodAndNote(mood: Int?, note: String?, onSaved: (() -> Unit)? = null) {
        viewModelScope.launch {
            val entry = _currentEntry.value
            if (entry != null) {
                val updated = entry.copy(mood = mood, note = note, updatedAt = System.currentTimeMillis())
                repo.insert(updated) // upsert via REPLACE
                _currentEntry.value = updated

                // Show confirmation message depending on whether a mood value was provided
                val msg = if (mood != null) {
                    "Estado de ánimo registrado correctamente"
                } else {
                    "Registro diario guardado con éxito"
                }

                Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
                onSaved?.invoke()
            }
        }
    }

    suspend fun getEntriesForMonth(year: Int, month: Int): List<DailyEntry> {
        val (start, end) = DateUtils.epochDayRangeForMonth(year, month)
        return repo.getBetween(start, end)
    }
}
