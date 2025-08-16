package com.example.notes_manager.core.network

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

object SecureRetrofitFactory {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun retrofit(
        context: Context,
        baseUrl: String,
        host: String,
        pins: List<String>
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(SecureNetwork.client(context, host, pins))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}
