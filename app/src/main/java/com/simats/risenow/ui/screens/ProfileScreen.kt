package com.simats.risenow.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.simats.risenow.ui.components.*
import com.simats.risenow.ui.theme.*

@Composable
fun ProfileScreen(
    userName: String,
    userIdentity: String = "",
    identityStatement: String = "",
    onEditSave: (name: String, statement: String) -> Unit = { _, _ -> },
    onTabSelected: (String) -> Unit,
    onPreferencesClick: () -> Unit = {},
    onReflectionClick: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var showEditDialog by remember { mutableStateOf(false) }

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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineMedium
                )

                // Settings icon → Preferences page
                IconButton(
                    onClick = onPreferencesClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- Avatar ---
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(GlassWhite, CircleShape)
                    .border(1.dp, GlassBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = PrimaryGradientStart,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Name ---
            Text(
                text = userName.ifBlank { "You" },
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- Identity badge ---
            if (userIdentity.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(GlassWhite)
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = userIdentity,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryGradientStart
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // --- Identity Statement Section ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "IDENTITY STATEMENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showEditDialog = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = PrimaryGradientStart,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Edit",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryGradientStart
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (identityStatement.isNotBlank())
                        "\"$identityStatement\""
                    else
                        "\"Tap Edit to write your identity statement.\"",
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    color = if (identityStatement.isNotBlank()) TextPrimary else TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Menu Options ---
            ProfileMenuCard(
                title = "Preferences",
                icon = Icons.Default.Settings,
                onClick = onPreferencesClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(16.dp))

            ProfileMenuCard(
                title = "Weekly Reflection",
                icon = Icons.Default.Edit,
                onClick = onReflectionClick
            )

            Spacer(modifier = Modifier.height(48.dp))

            // --- Sign Out ---
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .clickable { onSignOut() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = Color(0xFFEB5757),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out",
                    color = Color(0xFFEB5757),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(120.dp))
        }

        // --- Bottom Navigation ---
        BottomNavBar(
            currentTab = "profile",
            onTabSelected = onTabSelected,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // --- Edit Identity Dialog ---
    if (showEditDialog) {
        EditIdentityDialog(
            currentName = userName,
            currentStatement = identityStatement,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newStatement ->
                onEditSave(newName, newStatement)
                showEditDialog = false
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Edit Identity Bottom-Sheet-style Dialog
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun EditIdentityDialog(
    currentName: String,
    currentStatement: String,
    onDismiss: () -> Unit,
    onSave: (name: String, statement: String) -> Unit
) {
    var editedName by remember { mutableStateOf(currentName) }
    var editedStatement by remember { mutableStateOf(currentStatement) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(AppBackgroundTop.copy(alpha = 0.95f))
                    .border(1.dp, GlassBorder, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            ) {
                Column(modifier = Modifier.padding(28.dp)) {

                    // Handle bar
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(4.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Edit Your Identity",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "Changes reflect across the entire app.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Name field
                    Text(
                        text = "YOUR NAME",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        placeholder = { Text("Enter your name", color = Color.Gray.copy(alpha = 0.5f)) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryGradientStart)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGradientStart,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            cursorColor = PrimaryGradientStart,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = PrimaryGradientStart.copy(alpha = 0.05f),
                            unfocusedContainerColor = PrimaryGradientStart.copy(alpha = 0.03f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Identity Statement field
                    Text(
                        text = "WHO ARE YOU BECOMING?",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedStatement,
                        onValueChange = { editedStatement = it },
                        placeholder = {
                            Text(
                                "I am a disciplined leader who...",
                                color = Color.Gray.copy(alpha = 0.5f)
                            )
                        },
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGradientStart,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            cursorColor = PrimaryGradientStart,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = PrimaryGradientStart.copy(alpha = 0.05f),
                            unfocusedContainerColor = PrimaryGradientStart.copy(alpha = 0.03f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Char counter
                    Text(
                        text = "${editedStatement.length} chars",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        textAlign = TextAlign.End
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassButton(
                            text = "Cancel",
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        )

                        PrimaryButton(
                            text = "Save",
                            onClick = {
                                onSave(editedName.trim(), editedStatement.trim())
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Profile Menu Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ProfileMenuCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(GlassWhite)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
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
                        imageVector = icon,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.5f)
            )
        }
    }
}
