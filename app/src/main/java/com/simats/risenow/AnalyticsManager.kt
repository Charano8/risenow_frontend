package com.simats.risenow

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

data class WakeEvent(
    val actualTimeMillis: Long,
    val scheduledTimeMillis: Long,
    val date: String // yyyy-MM-dd
)

object AnalyticsManager {
    private const val PREFS_NAME = "RiseNowAnalytics"
    private const val KEY_WAKE_EVENTS = "wake_events_list"
    private const val KEY_WEEKLY_REFLECTION = "weekly_reflection_"
    private const val KEY_REMOTE_STREAK = "remote_streak"
    private const val KEY_REMOTE_WAKE_PERCENT = "remote_wake_percent"
    private const val KEY_REMOTE_WEEKLY_DATA = "remote_weekly_data"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun recordWakeUp(context: Context, actualTime: Long, scheduledTime: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val events = getWakeEvents(context).toMutableList()
        
        val dateStr = dateFormat.format(Date(actualTime))
        val newEvent = WakeEvent(actualTime, scheduledTime, dateStr)
        
        // Remove existing event for today if any (prevent double recording)
        events.removeAll { it.date == dateStr }
        events.add(newEvent)
        
        // Keep only last 30 days
        if (events.size > 30) {
            events.sortByDescending { it.actualTimeMillis }
            while (events.size > 30) events.removeAt(events.size - 1)
        }
        
        saveWakeEvents(prefs, events)
    }

    private fun saveWakeEvents(prefs: android.content.SharedPreferences, events: List<WakeEvent>) {
        val serialized = events.joinToString(";") { "${it.actualTimeMillis}|${it.scheduledTimeMillis}|${it.date}" }
        prefs.edit().putString(KEY_WAKE_EVENTS, serialized).apply()
    }

    fun getWakeEvents(context: Context): List<WakeEvent> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val serialized = prefs.getString(KEY_WAKE_EVENTS, null) ?: return emptyList()
        return serialized.split(";").filter { it.isNotBlank() }.map {
            val parts = it.split("|")
            WakeEvent(parts[0].toLong(), parts[1].toLong(), parts[2])
        }
    }

    fun getCurrentStreak(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.contains(KEY_REMOTE_STREAK)) {
            prefs.getInt(KEY_REMOTE_STREAK, 0)
        } else {
            val events = getWakeEvents(context).sortedByDescending { it.actualTimeMillis }
            if (events.isEmpty()) return 0
            
            var streak = 0
            for (event in events) {
                if (event.actualTimeMillis <= event.scheduledTimeMillis + 300000) {
                    streak++
                } else {
                    break
                }
            }
            streak
        }
    }

    fun getWakeConsistency(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.contains(KEY_REMOTE_WAKE_PERCENT)) {
            prefs.getInt(KEY_REMOTE_WAKE_PERCENT, 0)
        } else {
            val events = getWakeEvents(context)
            if (events.isEmpty()) return 0
            
            val onTimeCount = events.count { it.actualTimeMillis <= it.scheduledTimeMillis + 300000 }
            (onTimeCount * 100) / events.size
        }
    }

    fun getWeeklyPerformance(context: Context): List<Float> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val remoteData = prefs.getString(KEY_REMOTE_WEEKLY_DATA, null)
        if (remoteData != null) {
            return remoteData.split(",").map { it.toFloat() }
        }

        val events = getWakeEvents(context)
        val performance = MutableList(7) { 0f } // M, T, W, T, F, S, S
        
        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        
        events.forEach { event ->
            calendar.timeInMillis = event.actualTimeMillis
            if (calendar.get(Calendar.WEEK_OF_YEAR) == currentWeek) {
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                val index = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
                
                val delayMinutes = (event.actualTimeMillis - event.scheduledTimeMillis) / 60000
                val score = when {
                    delayMinutes <= 0 -> 1.0f
                    delayMinutes >= 60 -> 0.1f
                    else -> 1.0f - (delayMinutes.toFloat() / 60f) * 0.9f
                }
                performance[index] = score
            }
        }
        return performance
    }

    fun getAverageWakeTime(context: Context): String {
        val events = getWakeEvents(context)
        if (events.isEmpty()) return "--:--"
        
        var totalMinutes = 0L
        events.forEach { event ->
            val cal = Calendar.getInstance().apply { timeInMillis = event.actualTimeMillis }
            totalMinutes += cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        }
        
        val avgTotalMinutes = totalMinutes / events.size
        val avgHours = avgTotalMinutes / 60
        val avgMins = avgTotalMinutes % 60
        return String.format("%02d:%02d", avgHours, avgMins)
    }

    fun syncRemoteStats(context: Context, streak: Int, wakePercent: Int, weeklyData: List<Int>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_REMOTE_STREAK, streak)
            .putInt(KEY_REMOTE_WAKE_PERCENT, wakePercent)
            .putString(KEY_REMOTE_WEEKLY_DATA, weeklyData.joinToString(","))
            .apply()
    }

    data class WeeklySummary(
        val weekNumber: Int,
        val dateRange: String,
        val daysWoke: String,
        val consistencyChange: String,
        val bestStreak: Int,
        val mostConsistentTime: String,
        val reflection: String
    )

    fun getWeeklySummary(context: Context): WeeklySummary {
        val events = getWakeEvents(context)
        val calendar = Calendar.getInstance()
        
        // 1. Week Number and Date Range
        val weekNumber = calendar.get(Calendar.WEEK_OF_YEAR)
        
        // Set to Monday of current week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val startMonth = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
        val startDay = calendar.get(Calendar.DAY_OF_MONTH)
        
        // Set to Sunday
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endMonth = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
        val endDay = calendar.get(Calendar.DAY_OF_MONTH)
        
        val dateRange = "$startMonth $startDay - $endMonth $endDay"
        
        // 2. Days Woke (Unique days with events this week)
        calendar.set(Calendar.WEEK_OF_YEAR, weekNumber)
        val weeklyEvents = events.filter {
            val eventCal = Calendar.getInstance().apply { timeInMillis = it.actualTimeMillis }
            eventCal.get(Calendar.WEEK_OF_YEAR) == weekNumber
        }
        val uniqueDaysWoke = weeklyEvents.map { it.date }.distinct().size
        val daysWokeStr = "$uniqueDaysWoke/7"
        
        // 3. Consistency Change
        val lastWeekNumber = weekNumber - 1
        val lastWeeklyEvents = events.filter {
            val eventCal = Calendar.getInstance().apply { timeInMillis = it.actualTimeMillis }
            eventCal.get(Calendar.WEEK_OF_YEAR) == lastWeekNumber
        }
        
        fun calculateConsistency(ev: List<WakeEvent>): Float {
            if (ev.isEmpty()) return 0f
            val onTime = ev.count { it.actualTimeMillis <= it.scheduledTimeMillis + 300000 }
            return (onTime.toFloat() / ev.size.toFloat()) * 100
        }
        
        val currentConsistency = calculateConsistency(weeklyEvents)
        val lastConsistency = calculateConsistency(lastWeeklyEvents)
        val diff = currentConsistency - lastConsistency
        val consistencyChange = if (diff >= 0) "+${diff.toInt()}%" else "${diff.toInt()}%"
        
        // 4. Best Streak this week
        var bestStreak = 0
        var currentS = 0
        // Need to check day by day in order
        val sortedWeekly = weeklyEvents.sortedBy { it.actualTimeMillis }
        for (event in sortedWeekly) {
            if (event.actualTimeMillis <= event.scheduledTimeMillis + 300000) {
                currentS++
                if (currentS > bestStreak) bestStreak = currentS
            } else {
                currentS = 0
            }
        }

        // 5. Most Consistent Time
        val timeFreq = weeklyEvents.groupBy { 
            val cal = Calendar.getInstance().apply { timeInMillis = it.scheduledTimeMillis }
            String.format("%02d:%02d %s", 
                if (cal.get(Calendar.HOUR) == 0) 12 else cal.get(Calendar.HOUR),
                cal.get(Calendar.MINUTE),
                if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
            )
        }
        val mostConsistentTime = timeFreq.maxByOrNull { it.value.size }?.key ?: "--:-- AM"

        return WeeklySummary(
            weekNumber = weekNumber,
            dateRange = dateRange,
            daysWoke = daysWokeStr,
            consistencyChange = consistencyChange,
            bestStreak = bestStreak,
            mostConsistentTime = mostConsistentTime,
            reflection = getWeeklyReflection(context, weekNumber)
        )
    }

    fun saveWeeklyReflection(context: Context, weekNumber: Int, text: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_WEEKLY_REFLECTION + weekNumber, text).apply()
    }

    private fun getWeeklyReflection(context: Context, weekNumber: Int): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_WEEKLY_REFLECTION + weekNumber, "") 
            ?: "No reflection recorded yet. Tap to add one!"
    }
}
