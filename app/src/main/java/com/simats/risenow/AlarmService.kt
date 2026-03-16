package com.simats.risenow

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import androidx.core.app.NotificationCompat

import android.content.pm.ServiceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager

class AlarmService : Service() {
    companion object {
        const val ACTION_ALARM_TRIGGER = "com.simats.risenow.ALARM_TRIGGER"
        const val ACTION_STOP_ALARM = "com.simats.risenow.STOP_ALARM"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // We should ideally pause, but for an alarm, we might want to continue at lower volume
                // or just stay playing if it's critical. Professional apps usually keep playing.
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.setVolume(1.0f, 1.0f)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "RiseNow:AlarmServiceWakeLock"
        )
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_ALARM) {
            stopSelf()
            return START_NOT_STICKY
        }

        val label = intent?.getStringExtra("ALARM_LABEL") ?: "RiseNow Alarm"
        val time = intent?.getStringExtra("ALARM_TIME") ?: ""
        val period = intent?.getStringExtra("ALARM_PERIOD") ?: ""

        // 1. CALL STARTFOREGROUND IMMEDIATELY (within 5 seconds)
        val notification = createNotification(label, time, period)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14 (API 34)+ requires explicit foreground service types
            // Reverting to MEDIA_PLAYBACK for compatibility with current SDK configuration
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(1, notification)
        }
        
        AlarmUtils.setAlarmRinging(this, true)

        // 2. Perform background tasks (Sound/Vibration)
        Thread {
            try {
                // Ensure WakeLock is acquired immediately in the thread
                wakeLock?.acquire(10 * 60 * 1000L /*10 minutes*/)
                
                playAlarmSound()
                startVibration()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

        return START_STICKY
    }

    private fun createNotification(label: String, time: String, period: String): Notification {
        // Ensure channel is high importance
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(AlarmUtils.CHANNEL_ID, AlarmUtils.CHANNEL_NAME, importance).apply {
                description = AlarmUtils.CHANNEL_DESCRIPTION
                setSound(null, null)
                enableLights(true)
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Action when user clicks the notification or STOP button
        val stopIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("ALARM_LABEL", label)
            putExtra("ALARM_TIME", time)
            putExtra("ALARM_PERIOD", period)
        }
        val stopPendingIntent = PendingIntent.getActivity(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("ALARM_LABEL", label)
            putExtra("ALARM_TIME", time)
            putExtra("ALARM_PERIOD", period)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, AlarmUtils.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("RiseNow Alarm")
            .setContentText("Your alarm is ringing")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(stopPendingIntent)
            .addAction(0, "STOP", stopPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun playAlarmSound() {
        try {
            val volume = AlarmUtils.getAlarmVolume(applicationContext)
            mediaPlayer?.release()
            
            // Request Audio Focus
            requestAudioFocus()

            // Always use system default alarm sound
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setVolume(volume, volume)
                isLooping = true
                
                setOnErrorListener { _, what, extra ->
                    android.util.Log.e("AlarmService", "MediaPlayer error: $what, $extra")
                    false
                }

                prepare()
                start()
                android.util.Log.d("AlarmService", "MediaPlayer started with default sound at volume $volume.")
            }

        } catch (e: Exception) {
            android.util.Log.e("AlarmService", "Exception in playAlarmSound", e)
        }
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()

            audioManager?.requestAudioFocus(focusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            )
        }
    }

    private fun fallbackToDefaultSound(volume: Float) {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(applicationContext, alarmUri)?.apply {
                setVolume(volume, volume)
                isLooping = true
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun startVibration() {
        if (!AlarmUtils.isVibrationEnabled(applicationContext)) {
            android.util.Log.d("AlarmService", "Vibration disabled in preferences.")
            return
        }
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 500, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
        
        vibrator?.cancel()
        vibrator = null
        
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        
        // Explicitly remove the notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1) // 1 is the ID used in startForeground
        
        AlarmUtils.setAlarmRinging(this, false)
        android.util.Log.d("AlarmService", "AlarmService destroyed and resources released.")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
