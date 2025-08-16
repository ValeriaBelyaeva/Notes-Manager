package com.example.notes_manager.core.network

import android.content.Context
import com.example.notes_manager.core.serialization.JsonX
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

object RetrofitFactory {
    private val contentType = "application/json".toMediaType()

    fun retrofit(context: Context, baseUrl: String, json: Json = JsonX): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(Network.client(context))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}
