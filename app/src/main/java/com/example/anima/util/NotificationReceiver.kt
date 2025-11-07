package com.example.anima.util

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import android.app.PendingIntent

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "anima_reminder_channel"
        const val ACTION_REMIND = "com.example.anima.action.REMIND"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("NotificationReceiver", "onReceive called, intent=${intent}")
        val msg = intent?.getStringExtra("msg") ?: "¿Cómo te sentís hoy?"
        createChannelIfNeeded(context)

        // Record last fired timestamp for diagnostics
        try {
            val prefs = SecurePrefs(context)
            prefs.setLastReminderFired(System.currentTimeMillis())
        } catch (t: Throwable) {
            Log.w("NotificationReceiver", "Failed to record last fired timestamp", t)
        }

        // Optional visual confirmation for debugging (a short toast)
        try {
            Toast.makeText(context, "Recordatorio: $msg", Toast.LENGTH_SHORT).show()
        } catch (t: Throwable) {
            // ignore to avoid crashing receiver
            Log.w("NotificationReceiver", "Failed to show toast in receiver", t)
        }

        // Check notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.w("NotificationReceiver", "POST_NOTIFICATIONS not granted; skipping notify")
                return
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Anima")
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Make notification clickable: open the app's launch activity when tapped
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                val pending = PendingIntent.getActivity(
                    context,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.setContentIntent(pending)
            }
        } catch (t: Throwable) {
            // ignore; notification will still post without click action
        }

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(1000, builder.build())
                Log.d("NotificationReceiver", "Notification posted (id=1000)")
            }
        } catch (t: Throwable) {
            Log.e("NotificationReceiver", "Failed to post notification", t)
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
                    Log.d("NotificationReceiver", "Rescheduling next reminder for ${h}:${m}")
                    AlarmHelper.scheduleDailyReminder(context, h, m, message)
                }
            } catch (t: Throwable) {
                Log.e("NotificationReceiver", "Error rescheduling reminder", t)
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
            Log.d("NotificationReceiver", "Notification channel created/ensured: $CHANNEL_ID")
        }
    }
}
