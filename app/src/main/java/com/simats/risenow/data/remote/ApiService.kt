package com.simats.risenow.data.remote

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("register")
    fun registerUser(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("login")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @POST("forgot-password")
    fun forgotPassword(@Body request: ForgotPasswordRequest): Call<ForgotPasswordResponse>

    @POST("api/verify-otp")
    fun verifyOtp(@Body request: VerifyOtpRequest): Call<VerifyOtpResponse>

    @POST("api/reset-password")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<ResetPasswordResponse>

    @POST("api/add_alarm")
    fun addAlarm(@Body request: AddAlarmRequest): Call<AddAlarmResponse>

    @POST("api/update_streak")
    fun updateStreak(@Body request: UpdateStreakRequest): Call<ForgotPasswordResponse>

    @retrofit2.http.GET("api/get_streak")
    fun getStreak(@retrofit2.http.Query("user_id") userId: Int): Call<StreakResponse>
}
