package com.simats.risenow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            // Restore alarms and reminders from persistent storage
            AlarmUtils.rescheduleAllAlarms(context)
            AlarmUtils.scheduleBedtimeReminder(context)
        }
    }
}
