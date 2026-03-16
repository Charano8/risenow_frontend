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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.risenow.ui.components.*
import com.simats.risenow.ui.theme.*

@Composable
fun WeeklyReflectionScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scrollState = rememberScrollState()
    
    var summary by remember { mutableStateOf(com.simats.risenow.AnalyticsManager.getWeeklySummary(context)) }
    var showDialog by remember { mutableStateOf(false) }
    var reflectionText by remember { mutableStateOf(summary.reflection) }

    if (showDialog) {
        val darkSurface = Color(0xFF1A1F2C)
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = darkSurface,
            title = { Text("Weekly Reflection", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = reflectionText,
                    onValueChange = { reflectionText = it },
                    placeholder = { Text("How was your week?", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryGradientStart,
                        unfocusedBorderColor = GlassBorder
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    com.simats.risenow.AnalyticsManager.saveWeeklyReflection(context, summary.weekNumber, reflectionText)
                    summary = com.simats.risenow.AnalyticsManager.getWeeklySummary(context)
                    showDialog = false
                }) {
                    Text("Save", color = PrimaryGradientStart)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

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
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Weekly Reflection",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Week Title ---
            Text(
                text = "Week ${summary.weekNumber}",
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = summary.dateRange,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // --- Stats Cards ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    icon = Icons.Default.CalendarToday,
                    value = summary.daysWoke,
                    label = "Days Woke Up",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Default.TrendingUp,
                    value = summary.consistencyChange,
                    label = "Consistency",
                    modifier = Modifier.weight(1f),
                    iconColor = if (summary.consistencyChange.startsWith("+")) SuccessGreen else TextDisabled
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- Highlights Section ---
            SectionTitle("HIGHLIGHTS")
            
            HighlightCard(
                title = "Best Streak",
                description = "You hit a ${summary.bestStreak}-day streak this week! Keep it up for your personal best.",
                icon = Icons.Default.BookmarkBorder,
                accentColor = SuccessGreen
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            HighlightCard(
                title = "Most Consistent Time",
                description = "You were most consistent with your ${summary.mostConsistentTime} alarm.",
                icon = null,
                accentColor = Color.Transparent
            )

            Spacer(modifier = Modifier.height(40.dp))

            // --- Reflection Section ---
            SectionTitle("REFLECTION")
            
            GlassCard(
                modifier = Modifier.fillMaxWidth().clickable { 
                    reflectionText = if (summary.reflection.contains("Tap to add")) "" else summary.reflection
                    showDialog = true 
                }
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "LAST RECORDED",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "\"${summary.reflection}\"",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 18.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        color = TextPrimary.copy(alpha = 0.9f),
                        lineHeight = 28.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    iconColor: Color = PrimaryGradientStart
) {
    GlassCard(
        modifier = modifier.height(160.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall,
                    color = TextPrimary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun HighlightCard(
    title: String,
    description: String,
    icon: ImageVector?,
    accentColor: Color
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            if (accentColor != Color.Transparent) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(accentColor)
                )
            }
            
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
