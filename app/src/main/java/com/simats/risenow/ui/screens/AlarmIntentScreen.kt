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
fun AlarmIntentScreen(
    onBack: () -> Unit,
    onContinue: (String) -> Unit
) {
    var intentText by remember { mutableStateOf("") }
    val quickStarters = listOf("Fitness", "Career", "Learning", "Personal", "Health")
    val recentIntents = listOf(
        "Morning run 5k",
        "Finish project proposal",
        "Yoga and meditation"
    )

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

            // --- Title ---
            Text(
                text = "Why are you waking up?",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your intent drives your morning. Make it meaningful.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Intent Input Area ---
            OutlinedTextField(
                value = intentText,
                onValueChange = { if (it.length <= 60) intentText = it },
                placeholder = { 
                    Text(
                        "To crush my morning workout...", 
                        color = Color.Gray.copy(alpha = 0.4f)
                    ) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGradientStart,
                    unfocusedBorderColor = GlassBorder,
                    focusedContainerColor = GlassWhite,
                    unfocusedContainerColor = GlassWhite,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryGradientStart
                ),
                shape = RoundedCornerShape(24.dp)
            )
            
            Text(
                text = "${intentText.length}/60",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, end = 8.dp),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Quick Starters ---
            Text(
                text = "QUICK STARTERS",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    quickStarters.take(3).forEach { starter ->
                        StarterChip(
                            text = starter,
                            onClick = { intentText = starter },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    quickStarters.drop(3).forEach { starter ->
                        StarterChip(
                            text = starter,
                            onClick = { intentText = starter },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Spacer to align the second row if it has fewer items
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // --- Recent Intents ---
            Text(
                text = "RECENT INTENTS",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                recentIntents.forEach { intent ->
                    RecentIntentItem(
                        text = intent,
                        onClick = { intentText = intent }
                    )
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }

        // --- Continue Button ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {
            PrimaryButton(
                text = "Continue",
                onClick = { onContinue(intentText) },
                enabled = intentText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )

        }
    }
}

@Composable
fun StarterChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GlassWhite)
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )
    }
}

@Composable
fun RecentIntentItem(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassWhite)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = PrimaryGradientStart.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
