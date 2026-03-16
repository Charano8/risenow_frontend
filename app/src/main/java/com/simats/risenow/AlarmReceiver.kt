package com.simats.risenow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AlarmService.ACTION_ALARM_TRIGGER) {
            val label = intent.getStringExtra("ALARM_LABEL") ?: "Wake Up"
            val time = intent.getStringExtra("ALARM_TIME") ?: ""
            val period = intent.getStringExtra("ALARM_PERIOD") ?: ""

            // Strict checking: Verify alarm exists and is active in persistent storage
            val alarms = AlarmUtils.getAlarms(context)
            val matchedAlarm = alarms.find { it.time == time && it.period == period && it.isActive }
            
            if (matchedAlarm != null) {
                // Reschedule for next occurrence immediately (Everyday/Weekly)
                AlarmUtils.rescheduleAlarmAfterTrigger(context, matchedAlarm)

                val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                val isScreenOn = powerManager.isInteractive

                // 1. Always start Foreground Service for sound and (if screen on) notification
                val serviceIntent = Intent(context, AlarmService::class.java).apply {
                    action = AlarmService.ACTION_ALARM_TRIGGER
                    putExtra("ALARM_LABEL", label)
                    putExtra("ALARM_TIME", time)
                    putExtra("ALARM_PERIOD", period)
                }
                
                try {
                    ContextCompat.startForegroundService(context, serviceIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // 2. Launch AlarmRingingActivity DIRECTLY ONLY if screen is OFF
                if (!isScreenOn) {
                    val activityIntent = Intent(context, AlarmRingingActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("ALARM_LABEL", label)
                        putExtra("ALARM_TIME", time)
                        putExtra("ALARM_PERIOD", period)
                    }
                    context.startActivity(activityIntent)
                }
            }

        }
    }
}
