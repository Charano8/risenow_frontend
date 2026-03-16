package com.simats.risenow.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.risenow.ui.components.*
import com.simats.risenow.ui.theme.*

@Composable
fun AlarmRingingScreen(
    alarm: AlarmItem,
    ringtoneName: String,
    userIdentity: String = "",
    onPlaySound: () -> Unit,
    onStopSound: () -> Unit,
    onStopAlarm: () -> Unit
) {
    val scrollState = rememberScrollState()
    var userInput by remember { mutableStateOf("") }
    val intentRequirement = alarm.label.trim()
    val isIntentSet = intentRequirement.isNotBlank() && !intentRequirement.equals("Alarm", ignoreCase = true)
    val isValid = !isIntentSet || userInput.trim().equals(intentRequirement, ignoreCase = true)

    LaunchedEffect(Unit) { onPlaySound() }
    DisposableEffect(Unit) { onDispose { onStopSound() } }
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val quotes = remember {
        listOf(
            "The only way to do great work is to love what you do.",
            "Believe you can and you're halfway there.",
            "Discipline is the bridge between goals and accomplishment.",
            "Waking up early is the first victory of your day.",
            "Success is not final, failure is not fatal: it is the courage to continue that counts.",
            "Your future is created by what you do today, not tomorrow.",
            "The secret of getting ahead is getting started.",
            "Don't count the days, make the days count.",
            "Rise up and attack the day with enthusiasm!",
            "Small gains every day lead to big results over time.",
            "You don't have to be great to start, but you have to start to be great.",
            "The morning breeze has secrets to tell you. Don't go back to sleep.",
            "Motivation is what gets you started. Habit is what keeps you going.",
            "Energy and persistence conquer all things.",
            "Your potential is endless. Go do what you were created to do."
        )
    }
    val selectedQuote = remember { quotes.random() }

    val identityIcon = when (userIdentity.lowercase()) {
        "student"      -> Icons.Filled.MenuBook
        "professional" -> Icons.Filled.Work
        "entrepreneur" -> Icons.Filled.RocketLaunch
        "athlete"      -> Icons.Filled.DirectionsRun
        else           -> Icons.Filled.Work
    }
    val identityLabel = userIdentity.ifBlank { null }

    // 1. Slow-moving gradient background animation
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )

    // 2. Floating translateY loop animation
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatingOffset"
    )

    // 3. Pulsing glow animation for time text
    val textGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textGlowAlpha"
    )

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // --- Header ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onStopAlarm,
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
                text = "Wake up, $identityLabel!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.graphicsLayer(translationY = floatingOffset)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your morning starts now.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.graphicsLayer(translationY = floatingOffset)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Pulsing Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .graphicsLayer(translationY = floatingOffset)
                    .size(120.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp * scale)
                        .background(GlassWhite.copy(alpha = 0.2f), CircleShape)
                        .blur(10.dp)
                )
                Icon(
                    imageVector = identityIcon,
                    contentDescription = identityLabel,
                    tint = PrimaryGradientStart,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Time and Alarm Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer(translationY = floatingOffset)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Glow effect
                    Box(
                        modifier = Modifier
                            .size(240.dp, 100.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        PrimaryGradientEnd.copy(alpha = textGlowAlpha * 0.4f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Text(
                        text = "${alarm.time} ${alarm.period}",
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "ALARM RINGING",
                    style = MaterialTheme.typography.labelSmall,
                    color = DangerRed,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Ringtone Info Card
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassWhite)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = PrimaryGradientEnd,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ringtoneName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Motivational Quote Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(translationY = floatingOffset)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = PrimaryGradientStart.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(180f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = selectedQuote,
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp,
                        color = TextPrimary
                    )
                }
            }

            if (isIntentSet) {
                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "Confirm your intent",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Type \"$intentRequirement\" to stop the alarm",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Input Field
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    placeholder = { 
                        Text(
                            "Type here...", 
                            color = TextDisabled,
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isValid) SuccessGreen else PrimaryGradientStart,
                        unfocusedBorderColor = GlassBorder,
                        focusedContainerColor = GlassWhite,
                        unfocusedContainerColor = GlassWhite,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = PrimaryGradientStart
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Stop Button
            PrimaryButton(
                text = "Stop Alarm",
                onClick = onStopAlarm,
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
