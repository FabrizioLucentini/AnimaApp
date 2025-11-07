package com.example.anima.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

object AlarmHelper {
    private const val REQUEST_CODE_REMINDER = 1001
    private const val REQUEST_CODE_SHOW = 1002
    private const val REQUEST_CODE_TEST = 1003

    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int, message: String) {
        try {
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

            val trigger = computeNextTriggerCalendar(hour, minute)

            // debug log scheduled time
            val scheduledStr = "%04d-%02d-%02d %02d:%02d".format(
                trigger.get(Calendar.YEAR),
                trigger.get(Calendar.MONTH) + 1,
                trigger.get(Calendar.DAY_OF_MONTH),
                trigger.get(Calendar.HOUR_OF_DAY),
                trigger.get(Calendar.MINUTE)
            )
            Log.d("AlarmHelper", "Scheduling daily reminder for: $scheduledStr (epoch=${trigger.timeInMillis})")

            var usedAlarmClock = false
            try {
                // Try to use setAlarmClock for more reliable exact delivery
                val showIntent = Intent(context, com.example.anima.MainActivity::class.java)
                val showPending = PendingIntent.getActivity(
                    context,
                    REQUEST_CODE_SHOW,
                    showIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val info = AlarmManager.AlarmClockInfo(trigger.timeInMillis, showPending)
                alarmManager.setAlarmClock(info, pending)
                usedAlarmClock = true
                Log.d("AlarmHelper", "Scheduled using setAlarmClock for $scheduledStr")
            } catch (t: Throwable) {
                Log.w("AlarmHelper", "setAlarmClock failed, falling back to setExactAndAllowWhileIdle", t)
            }

            if (!usedAlarmClock) {
                // fallback
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    trigger.timeInMillis,
                    pending
                )
                Log.d("AlarmHelper", "Scheduled using setExactAndAllowWhileIdle for $scheduledStr")
            }

            // verify the pending intent exists now
            val exists = isReminderScheduled(context)
            Log.d("AlarmHelper", "Reminder PendingIntent exists after schedule: $exists")
        } catch (t: Throwable) {
            Log.e("AlarmHelper", "Failed to schedule daily reminder", t)
            // swallow to avoid crashing UI flows
        }
    }

    /**
     * Schedule an alarm at a specific epoch millisecond time for quick testing.
     * Returns true if scheduled without throwing.
     */
    fun scheduleAtMillis(context: Context, triggerMillis: Long, message: String): Boolean {
        return try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, com.example.anima.util.NotificationReceiver::class.java).apply {
                putExtra("msg", message)
            }
            val pending = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_TEST,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val scheduledStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(triggerMillis))
            Log.d("AlarmHelper", "Scheduling test alarm for: $scheduledStr (epoch=$triggerMillis)")

            var usedAlarmClock = false
            try {
                val showIntent = Intent(context, com.example.anima.MainActivity::class.java)
                val showPending = PendingIntent.getActivity(
                    context,
                    REQUEST_CODE_SHOW,
                    showIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val info = AlarmManager.AlarmClockInfo(triggerMillis, showPending)
                alarmManager.setAlarmClock(info, pending)
                usedAlarmClock = true
                Log.d("AlarmHelper", "Test alarm scheduled using setAlarmClock for $scheduledStr")
            } catch (t: Throwable) {
                Log.w("AlarmHelper", "setAlarmClock failed for test alarm, falling back", t)
            }

            if (!usedAlarmClock) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pending)
                Log.d("AlarmHelper", "Test alarm scheduled using setExactAndAllowWhileIdle for $scheduledStr")
            }

            true
        } catch (t: Throwable) {
            Log.e("AlarmHelper", "Failed to schedule test alarm", t)
            false
        }
    }

    fun cancelReminder(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, com.example.anima.util.NotificationReceiver::class.java)
            val pending = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_REMINDER,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pending)
            Log.d("AlarmHelper", "Cancelled reminder (PendingIntent canceled)")
        } catch (t: Throwable) {
            Log.e("AlarmHelper", "Failed to cancel reminder", t)
        }
    }

    fun isReminderScheduled(context: Context): Boolean {
        return try {
            val intent = Intent(context, com.example.anima.util.NotificationReceiver::class.java)
            val pending = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_REMINDER,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pending != null
        } catch (t: Throwable) {
            Log.e("AlarmHelper", "Error checking reminder pending intent", t)
            false
        }
    }

    // Compute and return the next trigger time in epoch millis for the given hour/minute
    fun computeNextTriggerMillis(hour: Int, minute: Int): Long {
        val cal = computeNextTriggerCalendar(hour, minute)
        return cal.timeInMillis
    }

    private fun computeNextTriggerCalendar(hour: Int, minute: Int): Calendar {
        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If trigger is not strictly after 'now' (equal or before), schedule for next day
            if (!after(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return trigger
    }
}
