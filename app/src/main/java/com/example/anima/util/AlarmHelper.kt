package com.example.anima.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object AlarmHelper {
    private const val REQUEST_CODE_REMINDER = 1001

    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int, message: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, com.example.anima.util.NotificationReceiver::class.java).apply {
            putExtra("msg", message)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // Use exact alarm when possible (compat with Doze)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            trigger.timeInMillis,
            pending
        )
    }

    fun cancelReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, com.example.anima.util.NotificationReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pending)
    }
}

