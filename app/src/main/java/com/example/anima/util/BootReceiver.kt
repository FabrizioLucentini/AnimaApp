package com.example.anima.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.anima.util.SecurePrefs
import com.example.anima.util.AlarmHelper

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_LOCKED_BOOT_COMPLETED) return
        try {
            val prefs = SecurePrefs(context)
            if (prefs.isReminderEnabled()) {
                AlarmHelper.scheduleDailyReminder(context, prefs.getReminderHour(), prefs.getReminderMinute(), prefs.getReminderMessage())
            }
        } catch (t: Throwable) {
            // ignore errors on boot handling
        }
    }
}
