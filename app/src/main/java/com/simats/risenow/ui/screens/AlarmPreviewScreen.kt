package com.simats.risenow.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.risenow.ui.components.*
import com.simats.risenow.ui.theme.*

@Composable
fun AlarmPreviewScreen(
    userName: String,
    alarmTime: String,
    intentText: String,
    alarmSchedule: String,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    onEdit: () -> Unit
) {

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
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
                        .size(40.dp)
                        .background(GlassWhite, CircleShape)
                        .border(1.dp, GlassBorder, CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // --- AI Preview Title ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "INTENTY AI PREVIEW",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryGradientStart,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- AI Message Card ---
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "\"$userName, tomorrow at $alarmTime you'll wake with purpose. Your intent is to ${intentText.lowercase()}. Remember — I am a disciplined leader who takes action....\"",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = TextPrimary.copy(alpha = 0.9f),
                    lineHeight = 32.sp,
                    modifier = Modifier.padding(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // --- Summary Section ---
            Text(
                text = "SUMMARY",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Time Row
                    SummaryRow(
                        icon = Icons.Default.AccessTime,
                        label = "ALARM TIME",
                        value = alarmTime
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 20.dp),
                        color = GlassBorder
                    )

                    // Intent Row
                    SummaryRow(
                        icon = Icons.Default.Adjust,
                        label = "INTENT",
                        value = intentText
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 20.dp),
                        color = GlassBorder
                    )

                    SummaryRow(
                        icon = Icons.Default.Event,
                        label = "SCHEDULE",
                        value = alarmSchedule
                    )
                }
            }


            Spacer(modifier = Modifier.height(120.dp))
        }

        // --- Bottom Buttons ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryButton(
                text = "Confirm Alarm",
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onEdit) {
                Text(
                    text = "Edit Details",
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun SummaryRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(GlassWhite, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(20.dp))
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = TextPrimary
            )
        }
    }
}
