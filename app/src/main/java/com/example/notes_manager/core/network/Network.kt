package com.example.notes_manager.core.network


import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

object Network {
    fun client(context: Context): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val cacheDir = File(context.cacheDir, "http-cache")
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .cache(Cache(cacheDir, 10L * 1024 * 1024)) // 10 MB
            // TODO: заменить на реальный токен или null
            .addInterceptor( AuthInterceptor { "my-secret-token"} )
            .addInterceptor(logging)
            .build()
    }
}