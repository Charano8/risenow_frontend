package com.simats.risenow

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.simats.risenow.ui.screens.AlarmItem
import java.util.*

data class RingtoneItem(val name: String, val resId: Int)

object AlarmUtils {
    const val CHANNEL_ID = "ALARM_CHANNEL_ID"
    const val CHANNEL_NAME = "RiseNow Alarms"
    const val CHANNEL_DESCRIPTION = "Channel for RiseNow alarm notifications"
    const val REMINDER_CHANNEL_ID = "BEDTIME_REMINDER_CHANNEL"
    const val REMINDER_CHANNEL_NAME = "Bedtime Reminders"
    private const val PREFS_NAME = "RiseNowAlarms"
    private const val KEY_ALARMS = "active_alarms_list"
    private const val KEY_IS_ALARM_RINGING = "is_alarm_ringing"
    private const val KEY_SELECTED_RINGTONE = "selected_ringtone_res_id"
    private const val KEY_SELECTED_RINGTONE_URI = "selected_ringtone_uri"
    private const val KEY_SELECTED_RINGTONE_NAME = "selected_ringtone_name"
    private const val KEY_SUNRISE_MODE = "sunrise_mode_enabled"
    private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
    private const val KEY_ALARM_VOLUME = "alarm_volume"
    private const val KEY_LOGIN_STATUS = "login_status"
    private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_IDENTITY = "user_identity"
    private const val KEY_IDENTITY_STATEMENT = "identity_statement"
    private const val KEY_USER_AGE = "user_age"
    private const val KEY_HISTORY = "alarm_history"
    private const val KEY_USER_ID = "user_id"



    // User Data Persistence
    fun saveUserData(context: Context, name: String, identity: String, statement: String, age: Int? = null, userId: String? = null) {
        val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        editor.putString(KEY_USER_NAME, name)
            .putString(KEY_USER_IDENTITY, identity)
            .putString(KEY_IDENTITY_STATEMENT, statement)
            .putInt(KEY_USER_AGE, age ?: -1)
        
        if (userId != null) {
            editor.putString(KEY_USER_ID, userId)
        }
        editor.apply()
    }

    fun getUserName(context: Context): String = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_USER_NAME, "") ?: ""
    fun getUserIdentity(context: Context): String = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_USER_IDENTITY, "") ?: ""
    fun getIdentityStatement(context: Context): String = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_IDENTITY_STATEMENT, "") ?: ""
    fun getUserAge(context: Context): Int = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_USER_AGE, -1)
    fun getUserId(context: Context): String = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_USER_ID, "guest_user") ?: "guest_user"

    fun setOnboardingComplete(context: Context, complete: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_ONBOARDING_COMPLETE, complete).apply()
    }

    fun isOnboardingComplete(context: Context): Boolean = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_ONBOARDING_COMPLETE, false)

    fun setLoginStatus(context: Context, loggedIn: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_LOGIN_STATUS, loggedIn).apply()
    }

    fun getLoginStatus(context: Context): Boolean = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_LOGIN_STATUS, false)


    fun isSunriseModeEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SUNRISE_MODE, true)
    }

    fun setSunriseModeEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_SUNRISE_MODE, enabled).apply()
    }

    fun isVibrationEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_VIBRATION_ENABLED, true)
    }

    fun setVibrationEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
    }

    fun getAlarmVolume(context: Context): Float {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_ALARM_VOLUME, 1.0f)
    }

    fun setAlarmVolume(context: Context, volume: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putFloat(KEY_ALARM_VOLUME, volume).apply()
    }

    fun setAlarmRinging(context: Context, ringing: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_IS_ALARM_RINGING, ringing).apply()
    }

    fun isAlarmRinging(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_IS_ALARM_RINGING, false)
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                setSound(null, null)
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            // Create silent reminder channel
            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Daily reminders to sleep on time"
                enableLights(true)
                enableVibration(false)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(reminderChannel)
        }
    }

    fun scheduleBedtimeReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            999, // Unique ID for reminder
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 22) // 10:00 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun scheduleAlarm(context: Context, alarm: AlarmItem) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmService.ACTION_ALARM_TRIGGER
            putExtra("ALARM_LABEL", alarm.label)
            putExtra("ALARM_TIME", alarm.time)
            putExtra("ALARM_PERIOD", alarm.period)
        }

        val requestCode = (alarm.time + alarm.period + alarm.label).hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = calculateNextOccurrence(alarm.time, alarm.period, alarm.schedule)

        // Use setExactAndAllowWhileIdle for maximum reliability
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
        
        saveAlarm(context, alarm.copy(isActive = true))
    }

    fun calculateNextOccurrence(time: String, period: String, schedule: String): Calendar {
        val calendar = Calendar.getInstance().apply {
            val parts = time.split(":")
            var hour = parts[0].toInt()
            val min = parts[1].toInt()
            if (period == "PM" && hour != 12) hour += 12
            if (period == "AM" && hour == 12) hour = 0
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, min)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val now = System.currentTimeMillis()
        
        if (schedule == "Everyday") {
            if (calendar.timeInMillis <= now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        } else if (schedule != "Everyday" && schedule != "Select days" && schedule != "Select date") {
            // Assume it's a comma-separated list of days or a single day (e.g., "Mon, Tue" or "Wed")
            val selectedDays = schedule.split(",").map { it.trim() }
            val dayMap = mapOf(
                "Sun" to Calendar.SUNDAY, "Mon" to Calendar.MONDAY, "Tue" to Calendar.TUESDAY,
                "Wed" to Calendar.WEDNESDAY, "Thu" to Calendar.THURSDAY, "Fri" to Calendar.FRIDAY, "Sat" to Calendar.SATURDAY
            )
            
            val targetDayRanks = selectedDays.mapNotNull { dayMap[it] }.sorted()
            if (targetDayRanks.isNotEmpty()) {
                val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
                
                // If today is a target day AND the time hasn't passed, use today.
                // Otherwise, find the next available target day.
                var found = false
                if (targetDayRanks.contains(currentDay) && calendar.timeInMillis > now) {
                    found = true
                }
                
                if (!found) {
                    // Find the next day in the list (could be next week)
                    var nextDay = -1
                    for (rank in targetDayRanks) {
                        if (rank > currentDay) {
                            nextDay = rank
                            break
                        }
                    }
                    
                    if (nextDay != -1) {
                        calendar.add(Calendar.DAY_OF_YEAR, nextDay - currentDay)
                    } else {
                        // Use the first day of next week
                        calendar.add(Calendar.DAY_OF_YEAR, 7 - (currentDay - targetDayRanks[0]))
                    }
                }
            } else {
                // Fallback for one-time or unknown
                if (calendar.timeInMillis <= now) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        } else {
            // Monthly or one-time handled as simple next-future-occurrence
            if (calendar.timeInMillis <= now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        return calendar
    }

    fun rescheduleAlarmAfterTrigger(context: Context, alarm: AlarmItem) {
        if (alarm.schedule == "Everyday" || (alarm.schedule != "Everyday" && alarm.schedule != "Select days" && !alarm.schedule.contains("Select date"))) {
            // It's a repeating alarm, schedule the next one
            scheduleAlarm(context, alarm)
        } else {
            // One-time alarm, turn it off
            saveAlarm(context, alarm.copy(isActive = false))
        }
    }

    private fun getAlarmCalendar(alarm: AlarmItem): Calendar {
        return Calendar.getInstance().apply {
            val parts = alarm.time.split(":")
            var hour = parts[0].toInt()
            val min = parts[1].toInt()
            if (alarm.period == "PM" && hour != 12) hour += 12
            if (alarm.period == "AM" && hour == 12) hour = 0
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, min)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    fun cancelAlarm(context: Context, alarm: AlarmItem) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmService.ACTION_ALARM_TRIGGER
            putExtra("ALARM_LABEL", alarm.label)
            putExtra("ALARM_TIME", alarm.time)
            putExtra("ALARM_PERIOD", alarm.period)
        }
        val requestCode = (alarm.time + alarm.period + alarm.label).hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        saveAlarm(context, alarm.copy(isActive = false))
    }

    fun deleteAlarm(context: Context, alarm: AlarmItem) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmService.ACTION_ALARM_TRIGGER
        }
        val requestCode = (alarm.time + alarm.period + alarm.label).hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val alarms = getAlarms(context).toMutableList()
        val index = alarms.indexOfFirst { it.time == alarm.time && it.period == alarm.period }
        if (index != -1) {
            alarms.removeAt(index)
            saveAlarmsList(prefs, alarms)
        }
    }

    private fun saveAlarm(context: Context, alarm: AlarmItem) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val alarms = getAlarms(context).toMutableList()
        val index = alarms.indexOfFirst { it.time == alarm.time && it.period == alarm.period }
        if (index != -1) alarms[index] = alarm else alarms.add(alarm)
        saveAlarmsList(prefs, alarms)
    }

    private fun saveAlarmsList(prefs: android.content.SharedPreferences, alarms: List<AlarmItem>) {
        val serialized = alarms.joinToString(";") { 
            "${it.time}|${it.period}|${it.label}|${it.schedule}|${it.isActive}" 
        }
        prefs.edit().putString(KEY_ALARMS, serialized).apply()
    }


    fun getAlarms(context: Context): List<AlarmItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val serialized = prefs.getString(KEY_ALARMS, null) ?: return emptyList()
        return serialized.split(";").filter { it.isNotBlank() }.map {
            val parts = it.split("|")
            AlarmItem(
                parts[0], parts[1], parts[2], parts[3], parts[4].toBoolean()
            )
        }

    }

    fun rescheduleAllAlarms(context: Context) {
        getAlarms(context).filter { it.isActive }.forEach { scheduleAlarm(context, it) }
    }

    fun cleanupStaleAlarms(context: Context) {
        val alarms = getAlarms(context)
        var updated = false
        val newAlarms = alarms.map { alarm ->
            if (alarm.isActive) {
                val cal = getAlarmCalendar(alarm)
                if (cal.timeInMillis < System.currentTimeMillis() - 60000) {
                    updated = true
                    alarm.copy(isActive = false)
                } else alarm
            } else alarm
        }
        if (updated) {
            saveAlarmsList(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE), newAlarms)
        }
    }

    fun addToHistory(context: Context, label: String, time: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val history = prefs.getString(KEY_HISTORY, "") ?: ""
        val entry = "${Calendar.getInstance().time} | $time | $label"
        val newHistory = if (history.isEmpty()) entry else "$entry\n$history"
        prefs.edit().putString(KEY_HISTORY, newHistory.take(5000)).apply()
    }
}

