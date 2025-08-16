package com.example.notes_manager.core.auth

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import com.example.notes_manager.data.api.AuthApi
import com.example.notes_manager.data.api.RefreshRequest

class TokenAuthenticator(
    private val authApi: AuthApi,            // отдельный клиент БЕЗ авторизации
    private val storage: TokenStorage
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        val req = response.request

        // уже пробовали? не повторяем второй раз
        if (req.header("X-Auth-Retry") == "1") return null

        // не крутимся на логине/рефреше
        val path = req.url.encodedPath
        if (path.contains("/auth/login") || path.contains("/auth/refresh")) return null

        // если у нас даже access/refresh нет — смысла нет
        val refresh = storage.currentRefresh() ?: return null

        // Один рефреш для пачки параллельных 401
        val refreshed = runBlocking {
            mutex.withLock {
                // могло обновиться пока мы ждали мутекс
                val stillValid = storage.currentAccess()?.isNotBlank() == true &&
                        req.header("Authorization") == null
                if (stillValid) return@withLock true

                runCatching {
                    val resp = authApi.refresh(RefreshRequest(refresh))
                    storage.setTokens(resp.accessToken, resp.refreshToken)
                    true
                }.getOrElse { false }
            }
        }

        if (!refreshed) return null

        val newAccess = storage.currentAccess() ?: return null

        // Повторяем исходный запрос один раз с новым токеном
        return req.newBuilder()
            .header("Authorization", "Bearer $newAccess")
            .header("X-Auth-Retry", "1")
            .build()
    }
}
