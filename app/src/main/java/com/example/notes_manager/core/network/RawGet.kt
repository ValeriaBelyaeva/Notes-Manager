package com.example.notes_manager.core.network

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.Request

data class RawHttp(
    val code: Int,
    val headers: Headers,
    val body: String
)

/** Делает именно GET и возвращает код/заголовки/тело даже при !2xx. */
suspend fun rawGet(context: Context, url: String): RawHttp =
    withContext(Dispatchers.IO) {
        val client = Network.client(context)
        val req = Request.Builder()
            .get()
            .url(url)
            .header("Accept", "application/json")
            .build()
        client.newCall(req).execute().use { r ->
            RawHttp(
                code = r.code,
                headers = r.headers,
                body = r.body?.string().orEmpty()
            )
        }
    }
