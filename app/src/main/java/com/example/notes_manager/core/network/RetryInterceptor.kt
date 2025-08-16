package com.example.notes_manager.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Простейший ретрай на сетевые ошибки (IOException).
 * Повторяет 1 раз (можно увеличить), если запрос упал до получения Response.
 */
class RetryInterceptor(
    private val maxRetries: Int = 1
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var lastError: IOException? = null
        while (attempt <= maxRetries) {
            try {
                return chain.proceed(chain.request())
            } catch (e: IOException) {
                lastError = e
                if (attempt == maxRetries) break
                attempt++
            }
        }
        throw lastError ?: IOException("Network error with unknown cause")
    }
}
