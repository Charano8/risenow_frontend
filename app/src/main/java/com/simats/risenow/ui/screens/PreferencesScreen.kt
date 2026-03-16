package com.simats.risenow.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import com.simats.risenow.AlarmUtils
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.risenow.ui.components.*
import com.simats.risenow.ui.theme.*

@Composable
fun PreferencesScreen(
    onBack: () -> Unit,
    onHelpClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scrollState = rememberScrollState()
    
    // States for toggles
    var sunriseMode by remember { mutableStateOf<Boolean>(AlarmUtils.isSunriseModeEnabled(context)) }
    var vibration by remember { mutableStateOf<Boolean>(AlarmUtils.isVibrationEnabled(context)) }
    var alarmVolume by remember { mutableStateOf<Float>(AlarmUtils.getAlarmVolume(context)) }

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
                    text = "Preferences",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Alarm Experience Section ---
            PreferenceSectionTitle("ALARM EXPERIENCE")
            
            PreferenceToggleCard(
                title = "Sunrise Mode",
                subtitle = "Gradual screen brightening",
                icon = Icons.Default.LightMode,
                checked = sunriseMode,
                onCheckedChange = { 
                    sunriseMode = it 
                    AlarmUtils.setSunriseModeEnabled(context, it)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PreferenceToggleCard(
                title = "Vibration",
                subtitle = "Haptic feedback",
                icon = Icons.Default.Smartphone,
                checked = vibration,
                onCheckedChange = { 
                    vibration = it 
                    AlarmUtils.setVibrationEnabled(context, it)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Sound Section ---
            PreferenceSectionTitle("SOUND")
            
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(GlassWhite, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Alarm Volume",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Slider(
                        value = alarmVolume,
                        onValueChange = { 
                            alarmVolume = it 
                        },
                        onValueChangeFinished = {
                            AlarmUtils.setAlarmVolume(context, alarmVolume)
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = PrimaryGradientStart,
                            inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- About Section ---
            PreferenceSectionTitle("ABOUT")
            
            AboutRow("Help & Support", onClick = onHelpClick)
            Spacer(modifier = Modifier.height(12.dp))
            AboutRow("Privacy Policy", onClick = onPrivacyClick)

            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "RiseNow v1.0.0 (Build 42)",
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PreferenceSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun PreferenceToggleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(GlassWhite, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
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

@Composable
fun AboutRow(title: String, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassWhite)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )
    }
}
