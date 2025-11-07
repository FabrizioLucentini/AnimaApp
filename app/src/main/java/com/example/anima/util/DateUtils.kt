package com.example.anima.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

    val formatter = SimpleDateFormat("d 'de' MMMM", Locale("es", "ES"))

    // Formatter for month + year, e.g. "noviembre 2025" in Spanish
    private val monthYearFormatter = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))

    /** Returns epoch day (days since 1970-01-01) for today's local date. */
    fun todayEpochDay(): Long {
        val cal = Calendar.getInstance()
        // set to local midnight
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val millis = cal.timeInMillis
        return Math.floorDiv(millis, MILLIS_PER_DAY)
    }

    /** Convert epochDay back to a Calendar instance in device default timezone. */
    fun calendarForEpochDay(epochDay: Long): Calendar {
        val cal = Calendar.getInstance()
        cal.timeZone = TimeZone.getDefault()
        cal.timeInMillis = epochDay * MILLIS_PER_DAY
        return cal
    }

    fun epochDayRangeForMonth(year: Int, month: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        // month in Calendar is 0-based
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = Math.floorDiv(cal.timeInMillis, MILLIS_PER_DAY)

        val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, lastDay)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val end = Math.floorDiv(cal.timeInMillis, MILLIS_PER_DAY)

        return start to end
    }

    /** Return localized "MMMM yyyy" for a given year and 1-based month (1..12) */
    fun formatMonthYear(year: Int, month: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return monthYearFormatter.format(cal.time)
    }
}