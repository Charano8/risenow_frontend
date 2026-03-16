package com.simats.risenow

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notification = NotificationCompat.Builder(context, AlarmUtils.REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("RiseNow Reminder")
            .setContentText("Sleep before 10:30 PM to achieve 8 hours of sleep.")
            .setPriority(NotificationCompat.PRIORITY_LOW) // Ensure it's silent/low priority
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(2, notification) // ID 2 for reminders
        
        // Reschedule for tomorrow
        AlarmUtils.scheduleBedtimeReminder(context)
    }
}
