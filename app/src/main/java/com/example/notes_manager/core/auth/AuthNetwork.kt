package com.example.notes_manager.core.network

import android.content.Context
import com.example.notes_manager.core.auth.AuthInterceptor
import com.example.notes_manager.core.auth.TokenAuthenticator
import com.example.notes_manager.core.auth.TokenStorage
import com.example.notes_manager.data.api.AuthApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

import okhttp3.MediaType.Companion.toMediaType
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

object AuthNetwork {

    fun authApi(context: Context, baseUrl: String): AuthApi {
        val retrofit = RetrofitFactory.retrofit(context, baseUrl) // обычный клиент без авторизации
        return retrofit.create(AuthApi::class.java)
    }

    fun authedClient(
        context: Context,
        authApi: AuthApi,
        storage: TokenStorage
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor { storage.currentAccess() })
            .authenticator(TokenAuthenticator(authApi, storage))
            .addInterceptor(logging)
            .build()
    }

    fun authedRetrofit(
        context: Context,
        baseUrl: String,
        authApi: AuthApi,
        storage: TokenStorage
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(authedClient(context, authApi, storage))
            .addConverterFactory(com.example.notes_manager.core.serialization.JsonX
                .asConverterFactory("application/json".toMediaType()))
            .build()
    }
}
