package com.simats.risenow

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.KeyguardManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import android.app.Activity
import android.os.PowerManager
import android.provider.Settings
import android.os.Build
import java.text.SimpleDateFormat
import java.util.*
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.runtime.*
import com.simats.risenow.ui.theme.RiseNowTheme
import com.simats.risenow.ui.screens.*
import com.simats.risenow.RingtoneItem
import com.simats.risenow.data.remote.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private fun stopAlarmSound() {
        stopService(Intent(this, AlarmService::class.java))
    }

    private fun createNotificationChannel() {
        AlarmUtils.createNotificationChannel(this)
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        // Check for Exact Alarm permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }

    private fun requestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = packageName
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }

    private fun scheduleSystemAlarm(alarm: AlarmItem) {
        AlarmUtils.scheduleAlarm(this, alarm)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        requestIgnoreBatteryOptimizations()
        
        enableEdgeToEdge()
        setContent {
            RiseNowTheme {
                // Initialize persistent state
                var showSplash by remember { 
                    mutableStateOf(!intent.getBooleanExtra("SKIP_SPLASH", false)) 
                }
                
                // Effect to handle splash delay
                LaunchedEffect(showSplash) {
                    if (showSplash) {
                        delay(2000)
                        showSplash = false
                    }
                }

                // Effect to cleanup stale alarms and RESCHEDULE all on launch
                LaunchedEffect(Unit) {
                    AlarmUtils.cleanupStaleAlarms(this@MainActivity)
                    AlarmUtils.rescheduleAllAlarms(this@MainActivity)
                    AlarmUtils.scheduleBedtimeReminder(this@MainActivity)
                }

                // Navigation State: Use a stack for standard Android back behavior
                val navigationStack = remember { 
                    val onboardingComplete = AlarmUtils.isOnboardingComplete(this@MainActivity)
                    val loggedIn = AlarmUtils.getLoginStatus(this@MainActivity)
                    
                    val initialScreen = when {
                        !loggedIn -> "onboarding" 
                        else -> "home"
                    }
                    mutableStateListOf(initialScreen) 
                }
                val currentScreen = navigationStack.lastOrNull() ?: "home"
                
                var backPressedTime by remember { mutableLongStateOf(0L) }
                
                BackHandler {
                    if (currentScreen == "home" || currentScreen == "onboarding") {
                        if (backPressedTime + 2000 > System.currentTimeMillis()) {
                            finish()
                        } else {
                            Toast.makeText(this@MainActivity, "Press back again to exit", Toast.LENGTH_SHORT).show()
                            backPressedTime = System.currentTimeMillis()
                        }
                    } else {
                        if (navigationStack.size > 1) {
                            navigationStack.removeAt(navigationStack.size - 1)
                        } else {
                            // Fallback to home if stack is empty
                            navigationStack.clear()
                            navigationStack.add("home")
                        }
                    }
                }

                fun navigateTo(screen: String, clearStack: Boolean = false) {
                    if (clearStack) {
                        navigationStack.clear()
                    }
                    // Avoid duplicate consecutive screens
                    if (navigationStack.lastOrNull() != screen) {
                        navigationStack.add(screen)
                    }
                }
                
                var userName by remember { mutableStateOf(AlarmUtils.getUserName(this@MainActivity)) }
                var userIdentity by remember { mutableStateOf(AlarmUtils.getUserIdentity(this@MainActivity)) }
                var identityStatement by remember { mutableStateOf(AlarmUtils.getIdentityStatement(this@MainActivity)) }
                var initialOnboardingPage by remember { mutableStateOf(0) }
                
                // State for current alarm flow
                var alarmTime by remember { mutableStateOf("06:30 AM") }
                var alarmHour by remember { mutableStateOf(6) }
                var alarmMinute by remember { mutableStateOf(30) }
                var alarmPeriod by remember { mutableStateOf("AM") }
                var alarmIntent by remember { mutableStateOf("") }
                var alarmSchedule by remember { mutableStateOf("Everyday") }
                
                // PERSISTENCE: Initialize alarmsList from storage
                var alarmsList by remember { mutableStateOf(AlarmUtils.getAlarms(this@MainActivity)) }
                var streak by remember { mutableStateOf(AnalyticsManager.getCurrentStreak(this@MainActivity)) }
                var wakePercentage by remember { mutableStateOf(AnalyticsManager.getWakeConsistency(this@MainActivity)) }
                var bedtimeReminderSet by remember { mutableStateOf(false) }
                var currentRingingAlarm by remember { mutableStateOf<AlarmItem?>(null) }

                // Handle onNewIntent triggers
                LaunchedEffect(intent) {
                    // Removed ALARM_TRIGGERED logic as it causes re-triggering on app resume.
                    // AlarmRingingActivity is handled directly by AlarmReceiver.
                }


                // Refresh data when returning to home
                LaunchedEffect(currentScreen) {
                    if (currentScreen == "home") {
                        streak = AnalyticsManager.getCurrentStreak(this@MainActivity)
                        wakePercentage = AnalyticsManager.getWakeConsistency(this@MainActivity)
                        alarmsList = AlarmUtils.getAlarms(this@MainActivity)
                    }
                }

                if (showSplash) {
                    SplashScreen()
                } else {

                    // Main App Content
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            val targetScreen = currentScreen
                            when (targetScreen) {
                                "login" -> LoginScreen(
                                    onLoginSuccess = { id, name, identity, statement ->
                                        userName = name
                                        userIdentity = identity
                                        identityStatement = statement
                                        AlarmUtils.setLoginStatus(this@MainActivity, true)
                                        AlarmUtils.setOnboardingComplete(this@MainActivity, true)
                                        // Save user data locally
                                        AlarmUtils.saveUserData(this@MainActivity, name, identity, statement, userId = id)
                                        navigateTo("subscription", clearStack = true)
                                    },
                                    onNavigateToSignup = {
                                        initialOnboardingPage = 6
                                        navigateTo("onboarding", clearStack = true)
                                    }
                                )
                                "onboarding" -> OnboardingScreen(
                                    initialPage = initialOnboardingPage,
                                    initialName = userName,
                                    initialIdentity = userIdentity,
                                    initialStatement = identityStatement,
                                    onComplete = { name, identity, statement ->
                                        userName = name
                                        userIdentity = identity
                                        identityStatement = statement
                                        val age = AlarmUtils.getUserAge(this@MainActivity)
                                        AlarmUtils.saveUserData(this@MainActivity, name, identity, statement, if (age == -1) null else age)
                                        AlarmUtils.setLoginStatus(this@MainActivity, true)
                                        AlarmUtils.setOnboardingComplete(this@MainActivity, true)
                                        navigateTo("subscription", clearStack = true)
                                    },
                                    onNavigateToLogin = {
                                        AlarmUtils.setOnboardingComplete(this@MainActivity, true)
                                        navigateTo("login", clearStack = true)
                                    },
                                    onRegistrationSuccess = {
                                        navigateTo("login", clearStack = true)
                                    }
                                )
                                "subscription" -> SubscriptionScreen(
                                    onNavigateHome = { navigateTo("home", clearStack = true) }
                                )
                                "home" -> HomeScreen(
                                    userName = userName,
                                    identityStatement = identityStatement,
                                    nextAlarm = alarmsList.firstOrNull(),
                                    streak = streak,
                                    wakePercentage = wakePercentage,
                                    onTabSelected = { tab -> navigateTo(tab) },
                                    onSleepSuggestionClick = { navigateTo("sleep_suggestion") },
                                    onProfileClick = { navigateTo("profile") },
                                    onAddAlarm = { 
                                        navigateTo("set_alarm") 
                                    }
                                )

                                "alarms" -> AlarmsScreen(
                                    alarms = alarmsList,
                                    onTabSelected = { tab -> navigateTo(tab) },

                                    onAddAlarm = { 
                                        navigateTo("set_alarm") 
                                    },
                                    onToggleAlarm = { alarm, isActive ->

                                        if (isActive) {
                                            AlarmUtils.scheduleAlarm(this@MainActivity, alarm.copy(isActive = true))
                                        } else {
                                            AlarmUtils.cancelAlarm(this@MainActivity, alarm)
                                        }
                                        // Refresh list from storage
                                        alarmsList = AlarmUtils.getAlarms(this@MainActivity)
                                    },
                                    onDeleteAlarm = { alarm ->
                                        AlarmUtils.deleteAlarm(this@MainActivity, alarm)
                                        // Refresh list from storage
                                        alarmsList = AlarmUtils.getAlarms(this@MainActivity)
                                    },
                                    onTestAlarm = { alarm ->
                                        // Launch full alarm experience for testing
                                        val testIntent = Intent(this@MainActivity, AlarmRingingActivity::class.java).apply {
                                            putExtra("ALARM_LABEL", alarm.label + " (Test)")
                                            putExtra("ALARM_TIME", alarm.time)
                                            putExtra("ALARM_PERIOD", alarm.period)
                                        }
                                        startActivity(testIntent)
                                        
                                        // Also start service for sound/vibe
                                        val serviceIntent = Intent(this@MainActivity, AlarmService::class.java).apply {
                                            action = AlarmService.ACTION_ALARM_TRIGGER
                                            putExtra("ALARM_LABEL", alarm.label + " (Test)")
                                            putExtra("ALARM_TIME", alarm.time)
                                            putExtra("ALARM_PERIOD", alarm.period)
                                        }
                                        androidx.core.content.ContextCompat.startForegroundService(this@MainActivity, serviceIntent)
                                    }
                                )
                                "stats" -> AnalyticsScreen(
                                    onTabSelected = { tab -> navigateTo(tab) }
                                )
                                "profile" -> ProfileScreen(
                                    userName = userName,
                                    userIdentity = userIdentity,
                                    identityStatement = identityStatement,
                                    onEditSave = { newName, newStatement ->
                                        if (newName.isNotBlank()) userName = newName
                                        identityStatement = newStatement
                                    },
                                    onTabSelected = { tab -> navigateTo(tab) },
                                    onPreferencesClick = { navigateTo("preferences") },
                                    onReflectionClick = { navigateTo("weekly_reflection") },
                                    onSignOut = {
                                        userName = ""
                                        userIdentity = ""
                                        identityStatement = ""
                                        AlarmUtils.saveUserData(this@MainActivity, "", "", "")
                                        AlarmUtils.setLoginStatus(this@MainActivity, false)
                                        AlarmUtils.setOnboardingComplete(this@MainActivity, false)
                                        navigateTo("login", clearStack = true)
                                    }
                                )
                                "set_alarm" -> SetAlarmScreen(
                                    onBack = { 
                                        if (navigationStack.size > 1) navigationStack.removeAt(navigationStack.size - 1)
                                        else navigateTo("alarms")
                                    },
                                    onSetTime = { h, m, p -> 
                                        alarmHour = h
                                        alarmMinute = m
                                        alarmPeriod = p
                                        alarmTime = "%02d:%02d".format(h, m)
                                        navigateTo("alarm_intent") 
                                    }
                                )
                                "alarm_intent" -> AlarmIntentScreen(
                                    onBack = { 
                                        if (navigationStack.size > 1) navigationStack.removeAt(navigationStack.size - 1)
                                        else navigateTo("set_alarm")
                                    },
                                    onContinue = { intent ->
                                        alarmIntent = intent
                                        navigateTo("alarm_frequency")
                                    }
                                )
                                "alarm_frequency" -> AlarmFrequencyScreen(
                                    onBack = { 
                                        if (navigationStack.size > 1) navigationStack.removeAt(navigationStack.size - 1)
                                        else navigateTo("alarm_intent")
                                    },
                                    onContinue = { schedule ->
                                        alarmSchedule = schedule
                                        navigateTo("alarm_preview")
                                    }
                                )
                                "alarm_preview" -> AlarmPreviewScreen(

                                    userName = userName,
                                    alarmTime = "$alarmTime $alarmPeriod",
                                    intentText = alarmIntent,
                                    alarmSchedule = alarmSchedule,
                                    onBack = { 
                                        if (navigationStack.size > 1) navigationStack.removeAt(navigationStack.size - 1)
                                        else navigateTo("alarm_frequency")
                                    },
                                    onConfirm = {
                                        val newAlarm = AlarmItem(
                                            time = "%02d:%02d".format(alarmHour, alarmMinute),
                                            period = alarmPeriod,
                                            label = alarmIntent,
                                            schedule = alarmSchedule,
                                            isActive = true
                                        )
                                        alarmsList = alarmsList + newAlarm
                                        scheduleSystemAlarm(newAlarm)
                                        
                                        // Submit to backend
                                        val userIdString = AlarmUtils.getUserId(this@MainActivity)
                                        val userIdInt = if (userIdString == "guest_user") 0 else userIdString.toIntOrNull() ?: 0
                                        
                                        // Convert to 24-hour format: HH:MM
                                        val hour24 = if (alarmPeriod == "PM" && alarmHour < 12) alarmHour + 12 
                                                     else if (alarmPeriod == "AM" && alarmHour == 12) 0 
                                                     else alarmHour
                                        val time24h = "%02d:%02d".format(hour24, alarmMinute)

                                        // Map schedule to backend format
                                        val scheduleField = when(alarmSchedule.lowercase()) {
                                            "everyday" -> "daily"
                                            "weekly" -> "weekly"
                                            "monthly" -> "monthly"
                                            else -> "daily"
                                        }

                                        val request = AddAlarmRequest(
                                            userId = userIdInt,
                                            alarmTime = time24h,
                                            period = alarmSchedule.lowercase(),
                                            label = alarmIntent,
                                            schedule = scheduleField,
                                            isActive = true
                                        )
                                        
                                        RetrofitClient.instance.addAlarm(request).enqueue(object : retrofit2.Callback<AddAlarmResponse> {
                                            override fun onResponse(call: retrofit2.Call<AddAlarmResponse>, response: retrofit2.Response<AddAlarmResponse>) {
                                                if (response.isSuccessful) {
                                                    // Alarm stored on backend for streak tracking
                                                }
                                            }
                                            override fun onFailure(call: retrofit2.Call<AddAlarmResponse>, t: Throwable) {
                                                // Local alarm is already set
                                            }
                                        })

                                        // Final synchronization check
                                        alarmsList = AlarmUtils.getAlarms(this@MainActivity)
                                        navigateTo("alarms", clearStack = true)
                                    },
                                    onEdit = { 
                                        if (navigationStack.size > 1) navigationStack.removeAt(navigationStack.size - 1)
                                        else navigateTo("set_alarm")
                                    }
                                )


                                "preferences" -> PreferencesScreen(
                                    onBack = { 
                                        if (navigationStack.size > 1) navigationStack.removeAt(navigationStack.size - 1)
                                        else navigateTo("profile")
                                    },
                                    onHelpClick = { navigateTo("help_support") },
                                    onPrivacyClick = { navigateTo("privacy_policy") }
                                )
                                "help_support" -> HelpSupportScreen(
                                    onBack = { 
                                        if (navigationStack.size > 1) navigationStack.removeAt(navigationStack.size - 1)
                                        else navigateTo("preferences")
                                    }
                                )
                                "privacy_policy" -> PrivacyPolicyScreen(
                                    onBack = { 
                                        if (navigationStack.size > 1) navigationStack.removeAt(navigationStack.size - 1)
                                        else navigateTo("preferences")
                                    }
                                )
                                "weekly_reflection" -> WeeklyReflectionScreen(
                                    onBack = { 
                                        if (navigationStack.size > 1) navigationStack.removeAt(navigationStack.size - 1)
                                        else navigateTo("profile")
                                    }
                                )

                                "sleep_suggestion" -> SleepSuggestionScreen(
                                    nextAlarm = alarmsList.firstOrNull(),
                                    isReminderSet = bedtimeReminderSet,
                                    onSetReminder = { bedtimeReminderSet = it },
                                    onBack = { 
                                        if (navigationStack.size > 1) navigationStack.removeAt(navigationStack.size - 1)
                                        else navigateTo("home")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
