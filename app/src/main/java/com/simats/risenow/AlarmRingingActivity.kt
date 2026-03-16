package com.simats.risenow

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.simats.risenow.ui.screens.AlarmItem
import com.simats.risenow.ui.screens.AlarmRingingScreen
import com.simats.risenow.ui.theme.RiseNowTheme
import com.simats.risenow.data.remote.*
import java.util.Calendar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AlarmRingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable full-screen activity over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        enableEdgeToEdge()

        // Disable back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing to prevent user from dismissing alarm with back button
            }
        })

        val label = intent.getStringExtra("ALARM_LABEL") ?: "RiseNow Alarm"
        val time = intent.getStringExtra("ALARM_TIME") ?: ""
        val period = intent.getStringExtra("ALARM_PERIOD") ?: ""
        val alarm = AlarmItem(time, period, label, "Everyday", true)
        val userIdentity = AlarmUtils.getUserIdentity(this)

        val isSunriseMode = AlarmUtils.isSunriseModeEnabled(this)
        
        setContent {
            RiseNowTheme {
                if (isSunriseMode) {
                    LaunchedEffect(Unit) {
                        val layoutParams = window.attributes
                        layoutParams.screenBrightness = 0.01f // Start very dim
                        window.attributes = layoutParams
                        
                        // Gradually increase brightness over 30 seconds
                        for (i in 1..100) {
                            kotlinx.coroutines.delay(300) // 300ms * 100 = 30 seconds
                            layoutParams.screenBrightness = i / 100f
                            window.attributes = layoutParams
                        }
                    }
                }

                AlarmRingingScreen(
                    alarm = alarm,
                    ringtoneName = "System Default",
                    userIdentity = userIdentity,
                    onPlaySound = {
                        // Sound is already started by AlarmService, but we can ensure it's ringing
                        val serviceIntent = Intent(this, AlarmService::class.java).apply {
                            action = AlarmService.ACTION_ALARM_TRIGGER
                            putExtra("ALARM_LABEL", label)
                            putExtra("ALARM_TIME", time)
                            putExtra("ALARM_PERIOD", period)
                        }
                        startService(serviceIntent)
                    },
                    onStopSound = {
                        // We don't stop sound here usually, wait for onStopAlarm
                    },
                    onStopAlarm = {
                        stopAlarm()
                    }
                )
            }
        }

    }

    private fun stopAlarm() {
        val serviceIntent = Intent(this, AlarmService::class.java)
        stopService(serviceIntent)
        
        // Also update persistence
        val label = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"
        val time = intent.getStringExtra("ALARM_TIME") ?: ""
        val period = intent.getStringExtra("ALARM_PERIOD") ?: ""
        
        AlarmUtils.setAlarmRinging(this, false)
        AlarmUtils.addToHistory(this, label, time)

        // Record for analytics
        if (time.isNotEmpty() && period.isNotEmpty()) {
            val calendar = Calendar.getInstance()
            val parts = time.split(":")
            if (parts.size == 2) {
                var hour = parts[0].toInt()
                val min = parts[1].toInt()
                if (period == "PM" && hour != 12) hour += 12
                if (period == "AM" && hour == 12) hour = 0
                
                val scheduledCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, min)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                // If the alarm was for "today" but it's already past (which it should be if it's ringing),
                // it might technically be tomorrow if it's after midnight, but usually it's the current date.
                // We just want the millis for today's version of that time.
                
                AnalyticsManager.recordWakeUp(
                    context = this,
                    actualTime = System.currentTimeMillis(),
                    scheduledTime = scheduledCal.timeInMillis
                )
            }
        }

        // --- NEW: Backend Streak Sync ---
        val userIdString = AlarmUtils.getUserId(this)
        val userIdInt = if (userIdString == "guest_user") 0 else userIdString.toIntOrNull() ?: 0
        
        if (userIdInt != 0) {
            val apiService = RetrofitClient.instance
            // 1. Update Streak
            apiService.updateStreak(UpdateStreakRequest(userIdInt)).enqueue(object : Callback<ForgotPasswordResponse> {
                override fun onResponse(call: Call<ForgotPasswordResponse>, response: Response<ForgotPasswordResponse>) {
                    if (response.isSuccessful) {
                        // 2. Fetch Updated Stats
                        apiService.getStreak(userIdInt).enqueue(object : Callback<StreakResponse> {
                            override fun onResponse(call: Call<StreakResponse>, resp: Response<StreakResponse>) {
                                if (resp.isSuccessful && resp.body() != null) {
                                    val data = resp.body()!!
                                    AnalyticsManager.syncRemoteStats(
                                        this@AlarmRingingActivity,
                                        data.streak,
                                        data.wakePercentage,
                                        data.weeklyData
                                    )
                                }
                                finish()
                            }
                            override fun onFailure(call: Call<StreakResponse>, t: Throwable) {
                                finish()
                            }
                        })
                    } else {
                        finish()
                    }
                }
                override fun onFailure(call: Call<ForgotPasswordResponse>, t: Throwable) {
                    finish()
                }
            })
        } else {
            finish()
        }
    }
}
