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
import com.simats.risenow.AnalyticsManager
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar

@Composable
fun HomeScreen(
    userName: String = "",
    identityStatement: String = "",
    nextAlarm: AlarmItem? = null,
    streak: Int = 0,
    wakePercentage: Int = 0,
    onTabSelected: (String) -> Unit = {},
    onAddAlarm: () -> Unit = {},
    onSleepSuggestionClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // Dynamic Greeting
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
    }

    // Real-time Countdown and Status
    var timeRemaining by remember { mutableStateOf("") }
    var alarmDayStatus by remember { mutableStateOf("Tomorrow") }
    
    LaunchedEffect(nextAlarm) {
        while (nextAlarm != null) {
            val now = Calendar.getInstance()
            val alarmCal = Calendar.getInstance().apply {
                val parts = nextAlarm.time.split(":")
                var h = parts[0].toInt()
                val m = parts[1].toInt()
                if (nextAlarm.period == "PM" && h != 12) h += 12
                if (nextAlarm.period == "AM" && h == 12) h = 0
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            if (alarmCal.before(now)) {
                alarmCal.add(Calendar.DAY_OF_YEAR, 1)
                alarmDayStatus = "Tomorrow"
            } else {
                alarmDayStatus = "Today"
            }
            
            val diff = alarmCal.timeInMillis - now.timeInMillis
            val hoursLeft = diff / (1000 * 60 * 60)
            val minsLeft = (diff / (1000 * 60)) % 60
            timeRemaining = if (hoursLeft > 0) "${hoursLeft}h ${minsLeft}m left" else "${minsLeft}m left"
            
            kotlinx.coroutines.delay(60000) // Update every minute
        }
    }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            HeaderSection(userName = userName, greeting = greeting, onProfileClick = onProfileClick)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            IdentityStatusCard(
                statement = identityStatement.ifBlank { "I am a disciplined leader who takes action." }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            AlarmDiscoverySection(
                nextAlarm = nextAlarm, 
                dayStatus = alarmDayStatus,
                timeRemaining = timeRemaining
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Streak",
                    value = streak.toString(),
                    icon = Icons.Default.Bolt,
                    containerColor = Color(0xFFFBBF24).copy(alpha = 0.07f),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Wake %",
                    value = "$wakePercentage%",
                    icon = Icons.Default.TrendingUp,
                    containerColor = PrimaryGradientStart.copy(alpha = 0.07f),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            SleepSuggestionCard(onClick = onSleepSuggestionClick)
            
            Spacer(modifier = Modifier.height(120.dp))
        }
        
        BottomNavBar(
            currentTab = "home",
            onTabSelected = onTabSelected,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        FloatingActionButton(
            onClick = onAddAlarm,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 24.dp)
                .size(64.dp),
            containerColor = PrimaryGradientStart,
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Alarm", modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun HeaderSection(userName: String, greeting: String, onProfileClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$greeting, ",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = PrimaryGradientStart
                )
            }
            Text(
                text = "Ready for tomorrow?",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(GlassWhite, CircleShape)
                .border(1.dp, GlassBorder, CircleShape)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = TextPrimary)
        }
    }
}

@Composable
fun IdentityStatusCard(statement: String) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = PrimaryGradientStart,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CURRENT IDENTITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryGradientStart,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "\"$statement\"",
                style = MaterialTheme.typography.titleLarge,
                lineHeight = 28.sp
            )
    }
}

@Composable
fun AlarmDiscoverySection(nextAlarm: AlarmItem?, dayStatus: String, timeRemaining: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Next Alarm",
                style = MaterialTheme.typography.titleLarge,
                color = PrimaryGradientEnd
            )
            if (nextAlarm != null) {
                Text(text = dayStatus, style = MaterialTheme.typography.bodyMedium)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (nextAlarm != null) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = nextAlarm.time,
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 64.sp
                )
                Text(
                    text = nextAlarm.period,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
                )
            }
            
            if (timeRemaining.isNotEmpty()) {
                Text(
                    text = timeRemaining,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryGradientStart,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (nextAlarm.label.isNotBlank()) {
                    MissionChip(text = nextAlarm.label)
                }
                MissionChip(text = "Focus")
            }
        } else {
            Text(
                text = "No alarms set",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
fun MissionChip(text: String) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(GlassWhite)
            .border(1.dp, GlassBorder, CircleShape)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryGradientEnd
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(GlassWhite)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .clickable { }
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
fun SleepSuggestionCard(onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassWhite)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(20.dp)
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
                    Icons.Filled.NightsStay,
                    contentDescription = null,
                    tint = PrimaryGradientStart,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sleep Suggestion",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Based on your recent activity...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

@Composable
fun BottomNavBar(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(24.dp)
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GlassWhite)
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(Icons.Filled.Home, isSelected = currentTab == "home", onClick = { onTabSelected("home") })
            NavItem(Icons.Filled.Schedule, isSelected = currentTab == "alarms", onClick = { onTabSelected("alarms") })
            NavItem(Icons.Filled.BarChart, isSelected = currentTab == "stats", onClick = { onTabSelected("stats") })
            NavItem(Icons.Filled.PersonOutline, isSelected = currentTab == "profile", onClick = { onTabSelected("profile") })
        }
    }
}

@Composable
fun NavItem(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.background(GlassWhite.copy(alpha = 0.2f))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) PrimaryGradientStart else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(PrimaryGradientStart, CircleShape)
                )
            }
        }
    }
}
