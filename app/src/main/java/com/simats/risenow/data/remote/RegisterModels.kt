package com.simats.risenow.data.remote

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("username") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("age") val age: Int? = null
)

data class RegisterResponse(
    @SerializedName("message") val message: String,
    @SerializedName("user_id") val userId: Int? = null
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("identity") val identity: String? = null,
    @SerializedName("identity_statement") val identityStatement: String? = null,
    @SerializedName("error") val error: String? = null
)

data class ForgotPasswordRequest(
    @SerializedName("email") val email: String
)

data class ForgotPasswordResponse(
    @SerializedName("message") val message: String
)

data class VerifyOtpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp") val otp: String
)

data class VerifyOtpResponse(
    @SerializedName("message") val message: String
)

data class ResetPasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp") val otp: String,
    @SerializedName("new_password") val newPassword: String
)

data class ResetPasswordResponse(
    @SerializedName("message") val message: String
)

data class AddAlarmRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("alarm_time") val alarmTime: String,
    @SerializedName("period") val period: String,
    @SerializedName("label") val label: String,
    @SerializedName("schedule") val schedule: String,
    @SerializedName("is_active") val isActive: Boolean
)

data class AddAlarmResponse(
    @SerializedName("message") val message: String,
    @SerializedName("alarm_id") val alarmId: Int? = null
)

data class UpdateStreakRequest(
    @SerializedName("user_id") val userId: Int
)

data class StreakResponse(
    @SerializedName("streak") val streak: Int,
    @SerializedName("wake_percentage") val wakePercentage: Int,
    @SerializedName("weekly_data") val weeklyData: List<Int>
)
