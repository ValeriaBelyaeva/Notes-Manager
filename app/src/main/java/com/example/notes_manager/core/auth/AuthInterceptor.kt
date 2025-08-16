package com.example.notes_manager.core.auth

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val path = req.url.encodedPath

        // не добавляем токен на эндпоинты логина/рефреша и если уже есть Authorization
        if (req.header("Authorization") != null ||
            path.contains("/auth/login") ||
            path.contains("/auth/refresh")
        ) {
            return chain.proceed(req)
        }

        val access = tokenProvider() ?: return chain.proceed(req)

        val withAuth = req.newBuilder()
            .header("Authorization", "Bearer $access")
            .build()

        return chain.proceed(withAuth)
    }
}
