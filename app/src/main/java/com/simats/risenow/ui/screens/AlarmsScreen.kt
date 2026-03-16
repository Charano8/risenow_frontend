package com.simats.risenow.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
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

data class AlarmItem(
    val time: String,
    val period: String,
    val label: String,
    val schedule: String,
    val isActive: Boolean
)


@Composable
fun AlarmsScreen(
    alarms: List<AlarmItem>,
    onTabSelected: (String) -> Unit,
    onAddAlarm: () -> Unit,
    onToggleAlarm: (AlarmItem, Boolean) -> Unit,
    onDeleteAlarm: (AlarmItem) -> Unit,
    onTestAlarm: (AlarmItem) -> Unit
) {

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // --- Header ---
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alarms",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                // Header Add Button
                IconButton(
                    onClick = onAddAlarm,
                    modifier = Modifier
                        .size(48.dp)
                        .background(GlassWhite, CircleShape)
                        .border(1.dp, GlassBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Alarm",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Alarm List ---
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(
                    items = alarms,
                    key = { _, alarm -> "${alarm.time}_${alarm.period}_${alarm.label}" }
                ) { _, alarm ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                onDeleteAlarm(alarm)
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                            val isSwiping = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
                            val color = if (isSwiping) Color(0xFFE53935) else Color.Transparent
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(color)
                                    .padding(horizontal = 24.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (isSwiping) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    ) {
                        AlarmCard(
                            alarm = alarm,
                            onTest = { onTestAlarm(alarm) },
                            onToggle = { isActive -> onToggleAlarm(alarm, isActive) }
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "\"Discipline is choosing between what you want now and what you want most.\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(140.dp))
                }
            }
        }

        // --- Bottom Navigation ---
        BottomNavBar(
            currentTab = "alarms",
            onTabSelected = onTabSelected,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // --- Floating Action Button ---
        FloatingActionButton(
            onClick = onAddAlarm,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 24.dp) // Consistent with Home screen
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
fun AlarmCard(
    alarm: AlarmItem,
    onToggle: (Boolean) -> Unit,
    onTest: () -> Unit
) {
    // Use the prop value directly if possible, or synchronize with parent
    val isActive = alarm.isActive

    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = alarm.time,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp),
                        color = if (isActive) TextPrimary else TextSecondary
                    )
                    Text(
                        text = alarm.period,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
                        color = if (isActive) TextPrimary.copy(alpha = 0.5f) else TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = alarm.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isActive) TextPrimary else TextSecondary
                )
                
                Text(
                    text = alarm.schedule,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = isActive,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryGradientStart,
                        uncheckedThumbColor = TextDisabled,
                        uncheckedTrackColor = GlassWhite,
                        uncheckedBorderColor = GlassBorder
                    )
                )
            }
        }
    }
}


