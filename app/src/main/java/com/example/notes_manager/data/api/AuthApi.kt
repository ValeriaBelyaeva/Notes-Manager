package com.example.notes_manager.data.api

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): TokenResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body req: RefreshRequest): TokenResponse
}
