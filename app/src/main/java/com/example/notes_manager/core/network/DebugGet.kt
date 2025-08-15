package com.example.notes_manager.core.network

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

suspend fun debugGet(context: Context, url: String): HttpDebug =
    withContext(Dispatchers.IO) {
        val client = Network.client(context)
        val req = Request.Builder()
            .get()
            .url(url)
            .header("Accept", "application/json")
            .build()

        client.newCall(req).execute().use { resp ->
            val code = resp.code
            val headers = resp.headers.toMultimap().mapValues { it.value.joinToString(", ") }
            val body = resp.body?.string().orEmpty()
            val slice = if (body.length > 200) body.substring(0, 200) else body

            if (code !in 200..299) {
                throw IllegalStateException("HTTP $code. First bytes: ${slice.replace("\n", " ")}")
            }
            HttpDebug(
                code = code,
                headers = headers,
                bodyFirst200 = slice
            )
        }
    }
