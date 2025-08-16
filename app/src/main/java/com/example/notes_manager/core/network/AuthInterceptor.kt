package com.example.notes_manager.core.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Добавляет Authorization: Bearer ... только к запросам на jsonplaceholder.typicode.com
 */
class AuthInterceptor(
    private val tokenProvider: () -> String? = { "test-123" }
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val host = req.url.host
        val token = tokenProvider()
        return if (host.equals("jsonplaceholder.typicode.com", ignoreCase = true) && !token.isNullOrBlank()) {
            val withAuth = req.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(withAuth)
        } else {
            chain.proceed(req)
        }
    }
}
