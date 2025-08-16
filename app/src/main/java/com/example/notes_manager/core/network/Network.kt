package com.example.notes_manager.core.network

import android.content.Context
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit
import com.example.notes_manager.core.etag.EtagInterceptor
import com.example.notes_manager.core.etag.EtagStore

/**
 * OkHttp с дисковым кэшем (10 МБ) + мягкое кэширование ответов без Cache-Control
 * + поддержка ETag/If-None-Match.
 */
object Network {
    private const val CACHE_SIZE = 10L * 1024 * 1024 // 10 MB

    /** Если сервер не дал Cache-Control/Expires/Pragma, аккуратно добавим public, max-age=60 (только для GET). */
    private class CacheRewriteInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val req = chain.request()
            val resp = chain.proceed(req)

            val hasCacheHeaders =
                resp.header("Cache-Control") != null ||
                        resp.header("Expires") != null ||
                        resp.header("Pragma") != null

            if (hasCacheHeaders) return resp
            if (!req.method.equals("GET", ignoreCase = true)) return resp

            return resp.newBuilder()
                .header("Cache-Control", "public, max-age=60, must-revalidate")
                .build()
        }
    }

    fun client(context: Context): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val cacheDir = File(context.cacheDir, "http-cache")

        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .cache(Cache(cacheDir, CACHE_SIZE))
            // 1) Сеть: подставим кэш-заголовок, если сервер молчит
            .addNetworkInterceptor(CacheRewriteInterceptor())
            // 2) Приложенческий: ETag/If-None-Match хранение/подстановка
            .addInterceptor(EtagInterceptor(EtagStore(context)))
            // 3) Логи
            .addInterceptor(logging)
            .build()
    }
}