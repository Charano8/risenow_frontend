package com.simats.risenow.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Note: 10.0.2.2 is the special IP address to access localhost from Android Emulator
    private const val BASE_URL = "http://10.20.123.96:5000/"
    // If testing on a physical device, use the machine's IP address (e.g., http://192.168.x.x:5000/)
    // The user specifically asked for http://127.0.0.1:5000, which on the device refers to the device itself.
    // I will provide a way to switch or default to a configurable one if needed, but for now 10.0.2.2 is standard for dev.

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}
