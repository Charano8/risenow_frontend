package com.simats.risenow.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun AlarmFrequencyScreen(
    onBack: () -> Unit,
    onContinue: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Everyday, 1: Weekly, 2: Monthly
    
    // Weekly state
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val selectedDays = remember { mutableStateListOf<String>() }
    
    // Monthly state
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    
    val currentSchedule = when (selectedTab) {
        0 -> "Everyday"
        1 -> if (selectedDays.isEmpty()) "Select days" else selectedDays.joinToString(", ")
        2 -> selectedDate?.let { "${it.dayOfMonth} ${it.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}" } ?: "Select date"
        else -> ""
    }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
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

            // --- Title ---
            Text(
                text = "When should it ring?",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Tab Selector ---
            TabSelector(
                tabs = listOf("Everyday", "Weekly", "Monthly"),
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Content Based on Selection ---
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> EverydayView()
                    1 -> WeeklyView(daysOfWeek, selectedDays)
                    2 -> MonthlyCalendarView(
                        selectedDate = selectedDate,
                        onDateSelected = { selectedDate = it }
                    )
                }
            }

            // --- Continue Button ---
            PrimaryButton(
                text = "Continue",
                onClick = { onContinue(currentSchedule) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
fun TabSelector(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(GlassWhite)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) PrimaryGradientStart else Color.Transparent)
                    .clickable { onTabSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                    color = if (isSelected) TextPrimary else TextSecondary
                )
            }
        }
    }
}

@Composable
fun EverydayView() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Repeat,
            contentDescription = null,
            tint = PrimaryGradientStart.copy(alpha = 0.5f),
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Alarm will ring every day",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Consistency is key to discipline.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun WeeklyView(days: List<String>, selectedDays: MutableList<String>) {
    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Text(
            text = "Select Days",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        days.forEach { day ->
            val isSelected = selectedDays.contains(day)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) PrimaryGradientStart.copy(alpha = 0.1f) else GlassWhite)
                    .border(1.dp, if (isSelected) PrimaryGradientStart else GlassBorder, RoundedCornerShape(16.dp))
                    .clickable { if (isSelected) selectedDays.remove(day) else selectedDays.add(day) }
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null, // Handled by surface click
                        colors = CheckboxDefaults.colors(
                            checkedColor = PrimaryGradientStart,
                            checkmarkColor = TextPrimary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyCalendarView(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val currentMonth = remember { YearMonth.now() }
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7 // 0 for Sunday
    
    val days = (1..daysInMonth).toList()
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + currentMonth.year,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Empty slots for days before the 1st
            items(firstDayOfMonth) {
                Box(modifier = Modifier.size(40.dp))
            }
            
            items(days) { day ->
                val date = currentMonth.atDay(day)
                val isSelected = selectedDate == date
                val isToday = date == LocalDate.now()
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected -> PrimaryGradientStart
                                isToday -> GlassWhite
                                else -> Color.Transparent
                            }
                        )
                        .clickable { onDateSelected(date) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        color = when {
                            isSelected -> TextPrimary
                            isToday -> PrimaryGradientStart
                            else -> TextSecondary
                        },
                        style = if (isSelected || isToday) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
