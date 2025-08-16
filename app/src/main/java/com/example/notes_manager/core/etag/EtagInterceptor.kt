package com.example.notes_manager.core.etag

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Для любых GET:
 *  - если знаем ETag по этому URL, добавляем If-None-Match
 *  - если сервер вернул ETag в ответе — сохраняем его
 * На 304 OkHttp автоматически отдаст тело из дискового кэша (если оно там есть).
 */
class EtagInterceptor(
    private val store: EtagStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var req = chain.request()

        if (req.method.equals("GET", ignoreCase = true) && req.header("If-None-Match") == null) {
            val saved = store.get(req.url.toString())
            if (!saved.isNullOrBlank()) {
                req = req.newBuilder()
                    .header("If-None-Match", saved)
                    .build()
            }
        }

        val resp = chain.proceed(req)

        resp.header("ETag")?.let { etag ->
            // Сохраняем ETag только для успешных GET-ответов
            if (req.method.equals("GET", ignoreCase = true) && resp.code in 200..299) {
                store.put(req.url.toString(), etag)
            }
        }

        return resp
    }
}
