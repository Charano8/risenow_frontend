package com.simats.risenow.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.risenow.ui.components.*
import com.simats.risenow.ui.theme.*
import com.simats.risenow.AnalyticsManager
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar

@Composable
fun AnalyticsScreen(onTabSelected: (String) -> Unit) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    val consistency = remember { AnalyticsManager.getWakeConsistency(context) }
    val weeklyPerformance = remember { AnalyticsManager.getWeeklyPerformance(context) }
    val avgWakeTime = remember { AnalyticsManager.getAverageWakeTime(context) }
    val currentStreak = remember { AnalyticsManager.getCurrentStreak(context) }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // --- Header ---
            Text(
                text = "Analytics",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Wake Consistency Card ---
            WakeConsistencyCard(consistency)

            Spacer(modifier = Modifier.height(24.dp))

            // --- Weekly Performance ---
            WeeklyPerformanceCard(weeklyPerformance)

            Spacer(modifier = Modifier.height(24.dp))

            // --- Stats Grid ---
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SmallStatCard(
                    title = avgWakeTime,
                    subtitle = "Avg. Wake Time",
                    icon = Icons.Filled.Schedule,
                    modifier = Modifier.weight(1f)
                )
                SmallStatCard(
                    title = currentStreak.toString(),
                    subtitle = "Current Streak",
                    icon = Icons.Filled.Bolt,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(120.dp)) // Padding for bottom nav
        }

        // --- Bottom Navigation ---
        BottomNavBar(
            currentTab = "stats",
            onTabSelected = onTabSelected,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun WakeConsistencyCard(consistency: Int) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Wake Consistency",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "$consistency%",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 42.sp),
                        color = TextPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                // Trend Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrendingUp,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(GlassWhite, CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (consistency > 0) consistency / 100f else 0.01f)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(PrimaryGradientStart, PrimaryGradientEnd)
                            ),
                            shape = CircleShape
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Top 5% of users this week",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun WeeklyPerformanceCard(performance: List<Float>) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly\nPerformance",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "This Week",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val days = listOf("M", "T", "W", "T", "F", "S", "S")
                days.forEachIndexed { index, day ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Dynamic bar
                        val score = performance.getOrElse(index) { 0f }
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(maxOf(4.dp, (score * 100).dp)) 
                                .background(
                                    if (score > 0) PrimaryGradientStart else GlassWhite, 
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SmallStatCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    GlassCard(
        modifier = modifier
            .height(160.dp)
            .clickable { /* Analytics detail */ }
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(GlassWhite, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryGradientStart,
                        modifier = Modifier.size(20.dp)
                    )
                }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.displaySmall,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
