package com.simats.risenow.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.risenow.ui.components.*
import com.simats.risenow.ui.theme.*
import java.util.Calendar


@Composable
fun SleepSuggestionScreen(
    nextAlarm: AlarmItem? = null,
    isReminderSet: Boolean = false,
    onSetReminder: (Boolean) -> Unit = {},
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // --- Header ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .background(GlassWhite, CircleShape)
                        .border(1.dp, GlassBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "Sleep Suggestion",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp)) // To balance header
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- Moon Icon Section ---
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Subtle stars background
                Canvas(modifier = Modifier.size(200.dp)) {
                    val points = listOf(
                        -80f to -60f, 60f to -80f, -40f to 40f, 95f to 20f, -100f to 10f
                    )
                    points.forEach { (x, y) ->
                        drawCircle(
                            color = Color.White.copy(alpha = 0.3f),
                            radius = 2.dp.toPx(),
                            center = center.copy(x = center.x + x.dp.toPx(), y = center.y + y.dp.toPx())
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(GlassWhite, CircleShape)
                        .border(1.dp, GlassBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.NightsStay,
                        contentDescription = null,
                        tint = PrimaryGradientStart,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Logic to calculate bedtime (8 hours before alarm)
            val bedtimeInfo = remember(nextAlarm) {
                if (nextAlarm == null) {
                    Triple("06 : 30", "AM", "No active alarm. Suggestions are for morning routines.")
                } else {
                    val timeParts = nextAlarm.time.split(":")
                    val h = timeParts[0].toInt()
                    val m = timeParts[1].toInt()
                    val isPm = nextAlarm.period == "PM"
                    
                    // Convert to 24h
                    val hour24 = if (isPm && h != 12) h + 12 else if (!isPm && h == 12) 0 else h
                    
                    // Check if morning alarm (4 AM to 10 AM)
                    val isMorningAlarm = hour24 in 4..10

                    if (!isMorningAlarm) {
                        Triple("${nextAlarm.time}", nextAlarm.period, "Sleep suggestions are only available for morning alarms (4:00 AM - 10:00 AM).")
                    } else {
                        val now = Calendar.getInstance()
                        val alarmCal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hour24)
                            set(Calendar.MINUTE, m)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        // If current time is past today's alarm time, it's "completed"
                        if (now.after(alarmCal)) {
                            Triple("DONE", "", "You have already completed 8 hours of sleep. Great job maintaining a healthy sleep schedule!")
                        } else {
                            // Subtract 8 hours
                            val bedtimeHour24 = (hour24 - 8 + 24) % 24
                            val bedtimePeriod = if (bedtimeHour24 >= 12) "PM" else "AM"
                            val bedtimeHour12 = if (bedtimeHour24 == 0) 12 else if (bedtimeHour24 > 12) bedtimeHour24 - 12 else bedtimeHour24
                            
                            Triple("%02d : %02d".format(bedtimeHour12, m), bedtimePeriod, "Based on your ${nextAlarm.time} ${nextAlarm.period} alarm")
                        }
                    }
                }
            }

            // --- Recommended Bedtime Card ---
            if (bedtimeInfo.first == "DONE") {
                MotivationalSleepCard(message = bedtimeInfo.third)
            } else {
                SleepMainCard(
                    time = bedtimeInfo.first,
                    period = bedtimeInfo.second,
                    reason = bedtimeInfo.third,
                    isRecommendationAvailable = bedtimeInfo.first != nextAlarm?.time
                )
            }


            Spacer(modifier = Modifier.height(24.dp))

            // --- Secondary Info Cards ---
            SleepInfoCard(
                icon = Icons.Filled.Bed,
                title = "TARGET DURATION",
                value = "8 hours",
                subtitle = "Includes 15 min to fall asleep"
            )

            Spacer(modifier = Modifier.height(16.dp))

            SleepInfoCard(
                icon = Icons.Filled.Schedule,
                title = "SLEEP CYCLES",
                value = "5 complete cycles",
                subtitle = "Wake during light sleep phase"
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Better Sleep Tips ---
            Text(
                text = "BETTER SLEEP TIPS",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            SleepTipCard(
                icon = Icons.Filled.Thermostat,
                text = "Keep room temperature between 60–67°F for optimal sleep."
            )
            Spacer(modifier = Modifier.height(12.dp))
            SleepTipCard(
                icon = Icons.Filled.Air,
                text = "Try the 4-7-8 breathing technique before bed."
            )
            Spacer(modifier = Modifier.height(12.dp))
            SleepTipCard(
                icon = Icons.Filled.PhonelinkOff,
                text = "Avoid screens 30 minutes before your target bedtime."
            )

            Spacer(modifier = Modifier.height(120.dp))
        }

        // --- Fixed Bottom Button ---
        PrimaryButton(
            text = if (isReminderSet) "Bedtime Reminder Set" else "Set Bedtime Reminder",
            onClick = { onSetReminder(!isReminderSet) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
fun SleepMainCard(time: String, period: String, reason: String, isRecommendationAvailable: Boolean) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
            Text(
                text = if (isRecommendationAvailable) "RECOMMENDED BEDTIME" else "NEXT ALARM",
                style = MaterialTheme.typography.labelSmall,
                color = PrimaryGradientStart,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 64.sp
                )
                if (period.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = period,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
    }
}

@Composable
fun MotivationalSleepCard(message: String) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Stars,
                contentDescription = null,
                tint = PrimaryGradientStart,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = TextPrimary,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "KEEP IT UP!",
                style = MaterialTheme.typography.labelLarge,
                color = PrimaryGradientStart,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}


@Composable
fun SleepInfoCard(icon: ImageVector, title: String, value: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(GlassWhite)
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(GlassWhite, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = PrimaryGradientStart, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(text = value, style = MaterialTheme.typography.titleLarge)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}

@Composable
fun SleepTipCard(icon: ImageVector, text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassWhite)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(GlassWhite, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
    }
}
