package com.simats.risenow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.risenow.ui.components.*
import com.simats.risenow.ui.theme.*
import com.simats.risenow.data.remote.*
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

enum class PasswordResetStep {
    ENTER_EMAIL,
    VERIFY_OTP,
    NEW_PASSWORD
}

@Composable
fun PasswordResetFlow(
    onBackToLogin: () -> Unit,
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(PasswordResetStep.ENTER_EMAIL) }
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (currentStep) {
            PasswordResetStep.ENTER_EMAIL -> {
                EnterEmailStep(
                    email = email,
                    onEmailChange = { email = it },
                    onContinue = { currentStep = PasswordResetStep.VERIFY_OTP },
                    onBackToLogin = onBackToLogin
                )
            }
            PasswordResetStep.VERIFY_OTP -> {
                VerifyOTPStep(
                    email = email,
                    otp = otp,
                    onOtpChange = { otp = it },
                    onVerify = { currentStep = PasswordResetStep.NEW_PASSWORD }
                )
            }
            PasswordResetStep.NEW_PASSWORD -> {
                NewPasswordStep(
                    email = email,
                    otp = otp,
                    password = newPassword,
                    onPasswordChange = { newPassword = it },
                    confirmPassword = confirmPassword,
                    onConfirmPasswordChange = { confirmPassword = it },
                    onUpdate = onComplete
                )
            }
        }
    }
}

@Composable
fun EnterEmailStep(
    email: String,
    onEmailChange: (String) -> Unit,
    onContinue: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Text(
        text = "Reset Password",
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Enter your email to receive a recovery link.",
        fontSize = 18.sp,
        color = Color.Gray,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(48.dp))

    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
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
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )

    Spacer(modifier = Modifier.height(32.dp))

    PrimaryButton(
        text = "Send Reset Code",
        isLoading = isLoading,
        onClick = {
            val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
            if (email.isNotBlank() && isEmailValid) {
                isLoading = true
                val request = ForgotPasswordRequest(email)
                RetrofitClient.instance.forgotPassword(request).enqueue(object : retrofit2.Callback<ForgotPasswordResponse> {
                    override fun onResponse(call: retrofit2.Call<ForgotPasswordResponse>, response: retrofit2.Response<ForgotPasswordResponse>) {
                        isLoading = false
                        if (response.isSuccessful) {
                            android.widget.Toast.makeText(context, response.body()?.message ?: "OTP sent to your email", android.widget.Toast.LENGTH_SHORT).show()
                            onContinue()
                        } else {
                            android.widget.Toast.makeText(context, "Error: ${response.code()}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<ForgotPasswordResponse>, t: Throwable) {
                        isLoading = false
                        android.widget.Toast.makeText(context, "Error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                })
            } else if (email.isNotBlank() && !isEmailValid) {
                android.widget.Toast.makeText(context, "Please enter a valid email address", android.widget.Toast.LENGTH_SHORT).show()
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = email.isNotBlank() && !isLoading
    )

    Spacer(modifier = Modifier.height(24.dp))

    TextButton(
        onClick = onBackToLogin,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(
            text = "Back to Login",
            color = Color.Gray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun VerifyOTPStep(
    email: String,
    otp: String,
    onOtpChange: (String) -> Unit,
    onVerify: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Text(
        text = "Verify OTP",
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Enter the 6-digit code sent to your email.",
        fontSize = 18.sp,
        color = Color.Gray,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(48.dp))

    OutlinedTextField(
        value = otp,
        onValueChange = { if (it.length <= 6) onOtpChange(it) },
        placeholder = { Text("6-digit code", color = Color.Gray.copy(alpha = 0.5f)) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF6C63FF),
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
    )

    Spacer(modifier = Modifier.height(32.dp))

    PrimaryButton(
        text = "Verify",
        isLoading = isLoading,
        onClick = {
            if (otp.length == 6) {
                isLoading = true
                val request = VerifyOtpRequest(email, otp)
                RetrofitClient.instance.verifyOtp(request).enqueue(object : retrofit2.Callback<VerifyOtpResponse> {
                    override fun onResponse(call: retrofit2.Call<VerifyOtpResponse>, response: retrofit2.Response<VerifyOtpResponse>) {
                        isLoading = false
                        if (response.isSuccessful) {
                            onVerify()
                        } else {
                            android.widget.Toast.makeText(context, "Invalid OTP", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<VerifyOtpResponse>, t: Throwable) {
                        isLoading = false
                        android.widget.Toast.makeText(context, "Error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                })
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = otp.length == 6 && !isLoading
    )
}

@Composable
fun NewPasswordStep(
    email: String,
    otp: String,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    onUpdate: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Text(
        text = "Create New Password",
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Enter your new password below.",
        fontSize = 18.sp,
        color = Color.Gray,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(48.dp))

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        placeholder = { Text("New Password", color = Color.Gray.copy(alpha = 0.5f)) },
        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.Gray) },
        trailingIcon = {
            val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val description = if (passwordVisible) "Hide password" else "Show password"
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = icon, contentDescription = description, tint = Color.Gray)
            }
        },
        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF6C63FF),
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password)
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = confirmPassword,
        onValueChange = onConfirmPasswordChange,
        placeholder = { Text("Confirm New Password", color = Color.Gray.copy(alpha = 0.5f)) },
        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.Gray) },
        trailingIcon = {
            val icon = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val description = if (confirmPasswordVisible) "Hide password" else "Show password"
            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                Icon(imageVector = icon, contentDescription = description, tint = Color.Gray)
            }
        },
        visualTransformation = if (confirmPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF6C63FF),
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password)
    )

    Spacer(modifier = Modifier.height(32.dp))

    PrimaryButton(
        text = "Update Password",
        isLoading = isLoading,
        onClick = {
            if (password.isNotBlank() && password == confirmPassword) {
                isLoading = true
                val request = ResetPasswordRequest(email, otp, password)
                RetrofitClient.instance.resetPassword(request).enqueue(object : retrofit2.Callback<ResetPasswordResponse> {
                    override fun onResponse(call: retrofit2.Call<ResetPasswordResponse>, response: retrofit2.Response<ResetPasswordResponse>) {
                        isLoading = false
                        if (response.isSuccessful) {
                            android.widget.Toast.makeText(context, response.body()?.message ?: "Password updated successfully", android.widget.Toast.LENGTH_SHORT).show()
                            onUpdate()
                        } else {
                            android.widget.Toast.makeText(context, "Error: ${response.code()}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<ResetPasswordResponse>, t: Throwable) {
                        isLoading = false
                        android.widget.Toast.makeText(context, "Error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                })
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = password.isNotBlank() && password == confirmPassword && !isLoading
    )
}
