package com.example.notes_manager.core.network

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

object Network {
    private const val CACHE_SIZE = 10L * 1024 * 1024 // 10 MB

    fun client(context: Context): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val cacheDir = File(context.cacheDir, "http-cache")

        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)   // свой ретрай контролируем сами
            .addInterceptor(AuthInterceptor { "test-123" }) // можно подменить лямбду реальным токеном
            .addInterceptor(RetryInterceptor(maxRetries = 1))
            .addInterceptor(logging)

            .cache(Cache(cacheDir, CACHE_SIZE))
            .build()
    }
}
