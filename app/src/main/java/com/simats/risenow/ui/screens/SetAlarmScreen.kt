package com.simats.risenow.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.risenow.ui.components.*
import com.simats.risenow.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SetAlarmScreen(onBack: () -> Unit, onSetTime: (Int, Int, String) -> Unit) {
    var selectedHour by remember { mutableStateOf(6) }
    var selectedMinute by remember { mutableStateOf(30) }
    var selectedPeriod by remember { mutableStateOf("AM") }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // --- Header ---
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onBack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Set Alarm Time",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.height(80.dp))

            // --- Rolling Time Picker ---
            TimePickerRolling(
                hour = selectedHour,
                minute = selectedMinute,
                period = selectedPeriod,
                onHourChange = { selectedHour = it },
                onMinuteChange = { selectedMinute = it },
                onPeriodChange = { selectedPeriod = it }
            )

            Spacer(modifier = Modifier.weight(1f))

            // --- Motivation ---
            Text(
                text = "Come on champ, you can do it!",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = TextPrimary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // --- Set Time Button ---
            PrimaryButton(
                text = "Set Time",
                onClick = { onSetTime(selectedHour, selectedMinute, selectedPeriod) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
fun TimePickerRolling(
    hour: Int,
    minute: Int,
    period: String,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onPeriodChange: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Hours
        RollingColumn(
            items = (1..12).toList(),
            initialItem = hour,
            onItemChange = { onHourChange(it) },
            modifier = Modifier.width(70.dp)
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 40.sp),
            color = TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Minutes
        RollingColumn(
            items = (0..59).toList(),
            initialItem = minute,
            onItemChange = { onMinuteChange(it) },
            modifier = Modifier.width(70.dp),
            format = { "%02d".format(it) }
        )

        Spacer(modifier = Modifier.width(32.dp))

        // AM/PM Selector - Static toggle to match screenshot
        AmPmSelector(
            selectedPeriod = period,
            onPeriodChange = onPeriodChange
        )
    }
}

@Composable
fun AmPmSelector(
    selectedPeriod: String,
    onPeriodChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .width(64.dp)
            .background(GlassWhite, RoundedCornerShape(16.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PeriodItem(
            text = "AM",
            isSelected = selectedPeriod == "AM",
            onClick = { onPeriodChange("AM") }
        )
        Spacer(modifier = Modifier.height(4.dp))
        PeriodItem(
            text = "PM",
            isSelected = selectedPeriod == "PM",
            onClick = { onPeriodChange("PM") }
        )
    }
}

@Composable
fun PeriodItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            color = if (isSelected) AppBackgroundTop else TextSecondary.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun <T> RollingColumn(
    items: List<T>,
    initialItem: T,
    onItemChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    format: (T) -> String = { it.toString() }
) {
    // Infinite list simulation
    val expandedItems = remember(items) { List(100) { items }.flatten() }
    val initialIndex = expandedItems.size / 2 + items.indexOf(initialItem)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex - 1)
    
    val coroutineScope = rememberCoroutineScope()

    // Better selection logic: check which item is closest to the center
    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) initialIndex
            else {
                val center = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                visibleItems.minByOrNull { 
                    kotlin.math.abs((it.offset + it.size / 2) - center)
                }?.index ?: initialIndex
            }
        }
    }

    // Update parent state when center index changes
    LaunchedEffect(centerIndex) {
        onItemChange(expandedItems[centerIndex % expandedItems.size])
    }

    // Auto-snapping
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val layoutInfo = listState.layoutInfo
            val center = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            val closestItem = layoutInfo.visibleItemsInfo.minByOrNull { 
                kotlin.math.abs((it.offset + it.size / 2) - center)
            }
            closestItem?.let {
                listState.animateScrollToItem(it.index - 1) // -1 to keep it centered
            }
        }
    }

    Box(
        modifier = modifier.height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            flingBehavior = ScrollableDefaults.flingBehavior()
        ) {
            items(expandedItems.size) { index ->
                val item = expandedItems[index]
                val isSelected = centerIndex == index
                
                // Distance from center for scaling/alpha
                val distance = kotlin.math.abs(centerIndex - index)
                val alpha = when (distance) {
                    0 -> 1f
                    1 -> 0.4f
                    else -> 0.1f
                }
                val scale = when (distance) {
                    0 -> 1.5f
                    1 -> 1.0f
                    else -> 0.8f
                }

                Text(
                    text = format(item),
                    fontSize = 32.sp,
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 32.sp),
                    color = if (isSelected) TextPrimary else TextSecondary,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .alpha(alpha)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                )
            }
        }
        
        // Horizontal lines to highlight center (premium feel)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalDivider(color = GlassBorder, thickness = 1.dp, modifier = Modifier.width(40.dp))
            Spacer(modifier = Modifier.height(60.dp))
            HorizontalDivider(color = GlassBorder, thickness = 1.dp, modifier = Modifier.width(40.dp))
        }
    }
}
