package com.simats.risenow.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.simats.risenow.ui.components.AppBackground
import com.simats.risenow.ui.theme.TextPrimary
import com.simats.risenow.ui.theme.TextSecondary
import com.simats.risenow.ui.components.GlassCard

@Composable
fun HelpSupportScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Help & Support",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    
                    // App Overview
                    Text(
                        text = "App Overview",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "RiseNow is designed to build discipline and wake consistency. It ensures you wake up on time by requiring you to complete an intent-based challenge to stop your alarm.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // How to Use the Alarm
                    Text(
                        text = "How to Use the Alarm",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Navigate to the Alarms tab.\n2. Tap the '+' button to schedule a new alarm.\n3. Enter your desired time and an intent or goal.\n4. Save the alarm to ensure it will ring at the scheduled time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // How to Stop the Alarm
                    Text(
                        text = "How to Stop the Alarm",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "When your alarm triggers, you simply cannot press a snooze button to stop it. Instead, you must correctly type the exact intent or challenge phrase you set for the alarm. Only then will the alarm be dismissed. This intentional friction builds discipline.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Troubleshooting
                    Text(
                        text = "Troubleshooting",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Alarm not ringing: Ensure your device volume is turned up and that RiseNow is allowed to override battery optimization restrictions.\n• Notification permission: Go to your device Settings > Apps > RiseNow > Permissions, and make sure Notifications and Exact Alarm privileges are granted.\n• Missing streak: Your streak updates only if you complete the intent challenge and stop the alarm properly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Contact Support
                    Text(
                        text = "Contact Support",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Still facing issues or have a suggestion? We're here to help. Contact us at:\ncharandevalapalli@gmail.com",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
