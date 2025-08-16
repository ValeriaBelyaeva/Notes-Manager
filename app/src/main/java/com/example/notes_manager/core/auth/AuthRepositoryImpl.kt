package com.example.notes_manager.data.auth

import android.content.Context
import com.example.notes_manager.core.auth.TokenStorage
import com.example.notes_manager.data.api.AuthApi
import com.example.notes_manager.data.api.LoginRequest
import com.example.notes_manager.data.api.TokenResponse
import com.example.notes_manager.core.network.RetrofitFactory

class AuthRepositoryImpl(
    context: Context,
    private val baseUrl: String
) : AuthRepository {

    private val storage = TokenStorage(context)

    // ВАЖНО: отдельный Retrofit/клиент без авторизации/автентикатора
    private val authApi: AuthApi = RetrofitFactory
        .retrofit(context, baseUrl)
        .create(AuthApi::class.java)

    override val isAuthorized = storage.isAuthorized

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val resp: TokenResponse = authApi.login(LoginRequest(email, password))
        storage.setTokens(resp.accessToken, resp.refreshToken)
    }

    override suspend fun logout() {
        storage.clear()
    }

    // Дадим доступ к storage и api для обслуживания рефреша (Authenticator)
    internal fun storage(): TokenStorage = storage
    internal fun authApi(): AuthApi = authApi
}
