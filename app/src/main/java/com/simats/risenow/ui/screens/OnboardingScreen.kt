package com.simats.risenow.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.risenow.ui.components.*
import com.simats.risenow.ui.theme.*
import com.simats.risenow.data.remote.*
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Data class for the first 3 simple pages
data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val buttonText: String
)

@Composable
fun OnboardingScreen(
    initialPage: Int = 0,
    initialName: String = "",
    initialIdentity: String? = null,
    initialStatement: String = "",
    onComplete: (name: String, identity: String, statement: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onRegistrationSuccess: () -> Unit = {}
) {
    val simplePages = listOf(
        OnboardingPage(
            title = "Set Intentions",
            description = "Start every morning with a clear purpose. No more groggy scrolling.",
            icon = Icons.Rounded.TrackChanges,
            color = OnboardingBlue,
            buttonText = "Next"
        ),
        OnboardingPage(
            title = "Build Identity",
            description = "Reinforce who you are becoming with daily affirmations and actions.",
            icon = Icons.Rounded.AccountCircle,
            color = OnboardingPurple,
            buttonText = "Next"
        ),
        OnboardingPage(
            title = "Track Growth",
            description = "Visualize your discipline and consistency with AI-powered insights.",
            icon = Icons.Rounded.TrendingUp,
            color = OnboardingIndigo,
            buttonText = "Get Started"
        )
    )

    // Total 8 Screens: 3 Intro + 1 Setup Transition + 1 Identity + 1 Statement + 1 Signup + 1 Success
    val totalPages = 8 
    var currentPageIndex by remember(initialPage) { mutableStateOf(initialPage) }
    val scrollState = rememberScrollState()
    
    // State for Name Input (Screen 4)
    var name by remember(initialName) { mutableStateOf(initialName) }
    
    // State for Age Input (Screen 5)
    var age by remember { mutableStateOf("") }
    var selectedIdentity by remember(initialIdentity) { mutableStateOf(initialIdentity) }
    val identities = listOf(
        "Student" to Icons.Filled.MenuBook,
        "Professional" to Icons.Filled.Work,
        "Entrepreneur" to Icons.Filled.RocketLaunch,
        "Athlete" to Icons.Filled.FitnessCenter,
        "Creator" to Icons.Filled.Palette
    )

    // State for Identity Statement (Screen 6)
    var identityStatement by remember(initialStatement) { mutableStateOf(initialStatement) }
    
    // Suggestion system for Identity Statement (Screen 7)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun isPasswordValid(pass: String): String? {
        if (pass.length < 8) return "Password must be at least 8 characters"
        if (!pass.any { it.isUpperCase() }) return "Password must contain at least one uppercase letter"
        if (!pass.any { it.isDigit() }) return "Password must contain at least one number"
        val specialChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?"
        if (!pass.any { it in specialChars }) return "Password must contain at least one special character"
        return null
    }

    AppBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Align content to top/center based on screen type
            verticalArrangement = if (currentPageIndex >= 3) Arrangement.Top else Arrangement.Center 
        ) {
            
            if (currentPageIndex >= 3) {
                Spacer(modifier = Modifier.height(48.dp)) // Top padding for input screens
            }

            if (currentPageIndex < 3) {
                // --- Intro Screens (0, 1, 2) ---
                val page = simplePages[currentPageIndex]
                
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .background(color = page.color.copy(alpha = 0.08f), shape = CircleShape)
                        .border(width = 1.dp, color = page.color.copy(alpha = 0.15f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(color = page.color.copy(alpha = 0.12f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = page.icon,
                            contentDescription = null,
                            tint = page.color,
                            modifier = Modifier.size(90.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text(text = page.title, style = MaterialTheme.typography.headlineMedium)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            } else if (currentPageIndex == 3) {
                // --- Screen 3: Setup Transition (Dot 1 of 6) ---
                Spacer(modifier = Modifier.height(100.dp))
                
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = PrimaryGradientStart,
                    modifier = Modifier.size(120.dp)
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Text(
                    text = "Let's personalize your experience",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    lineHeight = 44.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "A few steps to tailor RiseNow to your growth journey.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            } else if (currentPageIndex == 4) {
                // --- Screen 4: Identity Selection (Dot 2 of 5 in setup) ---
                Text(
                    text = "What defines your days?",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "We'll personalize your experience.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Identity Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IdentityCard(identities[0].first, identities[0].second, selectedIdentity == identities[0].first, { selectedIdentity = identities[0].first }, Modifier.weight(1f))
                    IdentityCard(identities[1].first, identities[1].second, selectedIdentity == identities[1].first, { selectedIdentity = identities[1].first }, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IdentityCard(identities[2].first, identities[2].second, selectedIdentity == identities[2].first, { selectedIdentity = identities[2].first }, Modifier.weight(1f))
                    IdentityCard(identities[3].first, identities[3].second, selectedIdentity == identities[3].first, { selectedIdentity = identities[3].first }, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    IdentityCard(identities[4].first, identities[4].second, selectedIdentity == identities[4].first, { selectedIdentity = identities[4].first }, Modifier.width(160.dp))
                }
            } else if (currentPageIndex == 5) {
                // --- Screen 5: Identity Statement (Dot 3 of 5 in setup) ---
                val suggestions = listOf(
                    "\"I am a disciplined leader who takes action.\"",
                    "\"I am a creative force building my legacy.\"",
                    "\"I am focused, calm, and unstoppable.\"",
                    "\"I am building a life of freedom and purpose.\""
                )

                Text(text = "Who are you becoming?", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Write your identity statement.\nThis will greet you every morning.", style = MaterialTheme.typography.bodyLarge, color = TextSecondary, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = identityStatement,
                    onValueChange = { identityStatement = it },
                    placeholder = { Text("I am a disciplined leader who...", color = Color.Gray.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGradientStart,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = GlassWhite,
                        unfocusedContainerColor = GlassWhite
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                
                Text(text = "${identityStatement.length} chars", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "SUGGESTIONS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.fillMaxWidth())
                
                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    suggestions.forEach { suggestion ->
                        SuggestionCard(text = suggestion, onClick = { identityStatement = suggestion })
                    }
                }
            } else if (currentPageIndex == 6) {
                // --- Screen 6: Signup (Dot 4 of 5 in setup) ---
                Text(
                    text = "Almost there",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Save your progress and sync across devices.",
                    fontSize = 18.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(48.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("What should we call you?", color = Color.Gray.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C63FF),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email address", color = Color.Gray.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C63FF),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password", color = Color.Gray.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.Gray) },
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = icon, contentDescription = null, tint = Color.Gray)
                        }
                    },
                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6C63FF),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password),
                    isError = passwordError != null
                )

                if (passwordError != null) {
                    Text(
                        text = passwordError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp).fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                val context = LocalContext.current

                PrimaryButton(
                    text = "Create Account",
                    isLoading = isLoading,
                    onClick = { 
                        passwordError = isPasswordValid(password)
                        val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                        if (email.isNotBlank() && isEmailValid && password.isNotBlank() && passwordError == null) {
                            isLoading = true
                            val request = RegisterRequest(
                                name = name.ifBlank { "Champion" }, 
                                email = email, 
                                password = password,
                                age = age.toIntOrNull()
                            )
                            RetrofitClient.instance.registerUser(request).enqueue(object : Callback<RegisterResponse> {
                                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                                    isLoading = false
                                    if (response.isSuccessful) {
                                        Toast.makeText(context, response.body()?.message ?: "User registered successfully", Toast.LENGTH_SHORT).show()
                                        onRegistrationSuccess()
                                    } else {
                                        val errorMsg = if (response.code() == 409) "Email already registered. Please log in." else "Registration failed: ${response.code()}"
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                                    isLoading = false
                                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                                }
                            })
                        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.isNotBlank()) {
                            Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                        } else if (passwordError == null) {
                            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Toggle to Login
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Already have an account? Log In",
                        color = Color(0xFF5B89F7),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
                
                Text(text = "By continuing you agree to our Terms & Privacy Policy", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            } else if (currentPageIndex == 7) {
                // --- Screen 7: Success (Dot 5 of 5 in setup) ---
                Spacer(modifier = Modifier.height(48.dp))
                
                Box(modifier = Modifier.size(120.dp).background(Color(0xFF6C63FF).copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF5B89F7), modifier = Modifier.size(80.dp))
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text(text = "You're all set, ${name.ifBlank { "Champion" }}!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Your journey begins now.", fontSize = 18.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(64.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "YOUR IDENTITY", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = null, tint = PrimaryGradientEnd.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = if (identityStatement.isNotBlank()) identityStatement else "\"I am a disciplined leader who takes action.\"", style = MaterialTheme.typography.titleLarge, lineHeight = 28.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(PrimaryGradientStart, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = selectedIdentity ?: "Student", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                }

                Spacer(modifier = Modifier.height(80.dp))

                PrimaryButton(text = "Begin Your Journey", onClick = { onComplete(name, selectedIdentity ?: "", identityStatement) }, modifier = Modifier.fillMaxWidth())
             }
            if (currentPageIndex >= 3) {
                 Spacer(modifier = Modifier.height(120.dp))
            }
        }

        // --- Bottom Section: Pager & Buttons ---
        Column(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)) {
            val canProceed = when {
                currentPageIndex == 4 -> selectedIdentity != null
                currentPageIndex == 5 -> identityStatement.isNotBlank()
                else -> true
            }

            if (currentPageIndex < 7) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    val indicatorCount = if (currentPageIndex < 3) 3 else 5
                    val activeIndex = if (currentPageIndex < 3) currentPageIndex else currentPageIndex - 3
                    
                    repeat(indicatorCount) { index ->
                        val isActive = index == activeIndex
                        val color = if (currentPageIndex < 3) {
                            if (isActive) simplePages[currentPageIndex].color else Color.Gray.copy(alpha = 0.3f)
                        } else {
                            if (isActive) Color(0xFF6C63FF) else Color.Gray.copy(alpha = 0.5f)
                        }
                        
                        Box(modifier = Modifier.height(6.dp).width(if (isActive) 24.dp else 6.dp).background(color = color, shape = if (isActive) RoundedCornerShape(3.dp) else CircleShape))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                if (currentPageIndex < 3) {
                    val isLastIntro = currentPageIndex == 2
                    TextButton(onClick = { onNavigateToLogin() }) { Text(text = "Skip", color = Color.Gray, fontSize = 16.sp) }
                    GlowingButton(
                        text = simplePages[currentPageIndex].buttonText, 
                        onClick = { 
                            if (isLastIntro) onNavigateToLogin() else currentPageIndex++ 
                        }, 
                        backgroundColor = simplePages[currentPageIndex].color
                    )
                } else if (currentPageIndex in 3..5) {
                    PrimaryButton(text = if (currentPageIndex == 4) "Confirm Identity" else "Continue", onClick = { if (canProceed) currentPageIndex++ }, modifier = Modifier.fillMaxWidth())
                }
            }

            if (currentPageIndex < 3) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Powered by SIMATS ENGINEERING",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
    }
}
}
}

@Composable
fun SuggestionCard(text: String, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(GlassWhite).border(1.dp, GlassBorder, RoundedCornerShape(12.dp)).clickable { onClick() }.padding(16.dp), contentAlignment = Alignment.CenterStart) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}

@Composable
fun IdentityCard(title: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.height(100.dp).clip(RoundedCornerShape(16.dp)).background(if (isSelected) PrimaryGradientStart.copy(alpha = 0.2f) else GlassWhite).border(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) PrimaryGradientStart else GlassBorder, shape = RoundedCornerShape(16.dp)).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) PrimaryGradientStart else TextSecondary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = if (isSelected) MaterialTheme.typography.titleLarge.copy(fontSize = 14.sp) else MaterialTheme.typography.bodyMedium, color = if (isSelected) TextPrimary else TextSecondary)
        }
    }
}

@Composable
fun GlowingButton(text: String, onClick: () -> Unit, enabled: Boolean = true, backgroundColor: Color = Color(0xFF6C63FF), modifier: Modifier = Modifier) {
    val brush = SolidColor(if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f))
    val finalModifier = if (modifier == Modifier) Modifier.height(56.dp).width(if (text.length > 5) 170.dp else 150.dp) else modifier

    Button(onClick = onClick, enabled = enabled, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent, disabledContentColor = Color.Gray.copy(alpha = 0.5f)), contentPadding = PaddingValues(0.dp), shape = RoundedCornerShape(18.dp), modifier = finalModifier.background(brush = brush, shape = RoundedCornerShape(18.dp))) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
             Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(text = text, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = if (enabled) Color.White else Color.Gray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = if (enabled) Color.White else Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Preview
@Composable
fun OnboardingScreenPreview() {
    RiseNowTheme {
        OnboardingScreen(
            onComplete = { _, _, _ -> },
            onNavigateToLogin = {}
        )
    }
}
