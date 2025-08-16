package com.example.notes_manager.data.auth

import android.content.Context
import com.example.notes_manager.core.auth.TokenStorage
import com.example.notes_manager.core.network.AuthNetwork
import com.example.notes_manager.core.network.RetrofitFactory
import com.example.notes_manager.data.api.AuthApi
import retrofit2.Retrofit

class AuthEnvironment(
    val storage: TokenStorage,
    val authApi: AuthApi,
    val authedRetrofit: Retrofit
)

fun buildAuthEnvironment(context: Context, baseUrl: String): AuthEnvironment {
    val repo = AuthRepositoryImpl(context, baseUrl) // содержит storage и authApi без авторизации
    val storage = repo.storage()
    val authApi = repo.authApi()
    val authedRetrofit = AuthNetwork.authedRetrofit(context, baseUrl, authApi, storage)
    return AuthEnvironment(storage, authApi, authedRetrofit)
}
