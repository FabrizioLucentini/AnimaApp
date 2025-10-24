package com.example.anima.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import java.util.concurrent.Executors

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "anima_reminder_channel"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val msg = intent?.getStringExtra("msg") ?: "¿Cómo te sentís hoy?"
        createChannelIfNeeded(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Anima")
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(1000, builder.build())
        }

        // Schedule next day to keep it daily (because we used exact alarm for a single trigger)
        // Read stored hour/minute and reschedule
        Executors.newSingleThreadExecutor().execute {
            try {
                val prefs = SecurePrefs(context)
                if (prefs.isReminderEnabled()) {
                    val h = prefs.getReminderHour()
                    val m = prefs.getReminderMinute()
                    val message = prefs.getReminderMessage()
                    AlarmHelper.scheduleDailyReminder(context, h, m, message)
                }
            } catch (t: Throwable) {
                // ignore
            }
        }
    }

    private fun createChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios"
            val descriptionText = "Canal para recordatorios diarios"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

