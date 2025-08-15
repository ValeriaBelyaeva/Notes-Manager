package com.example.notes_manager.core.network

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

private suspend fun sendJson(
    context: Context,
    method: String,               // "POST" | "PUT" | "PATCH"
    url: String,
    payloadJson: String
): HttpDebug = withContext(Dispatchers.IO) {
    val client = Network.client(context)
    val body = payloadJson.toRequestBody(JSON)
    val builder = Request.Builder()
        .url(url)
        .header("Accept", "application/json")
        .header("Content-Type", "application/json")
    val req = when (method) {
        "POST"  -> builder.post(body).build()
        "PUT"   -> builder.put(body).build()
        "PATCH" -> builder.patch(body).build()
        else -> error("Unsupported method $method")
    }

    client.newCall(req).execute().use { r ->
        val code = r.code
        val headers = r.headers.toMultimap().mapValues { it.value.joinToString(", ") }
        val txt = r.body?.string().orEmpty()
        val slice = if (txt.length > 200) txt.substring(0, 200) else txt
        if (code !in 200..299) throw IllegalStateException("HTTP $code. First bytes: ${slice.replace("\n"," ")}")
        HttpDebug(code, headers, slice)
    }
}

suspend fun postJson(context: Context, url: String, payloadJson: String): HttpDebug =
    sendJson(context, "POST", url, payloadJson)

suspend fun putJson(context: Context, url: String, payloadJson: String): HttpDebug =
    sendJson(context, "PUT", url, payloadJson)

suspend fun patchJson(context: Context, url: String, payloadJson: String): HttpDebug =
    sendJson(context, "PATCH", url, payloadJson)

suspend fun deleteRequest(context: Context, url: String): Int = withContext(Dispatchers.IO) {
    val client = Network.client(context)
    val req = Request.Builder()
        .url(url)
        .delete()
        .header("Accept", "application/json")
        .build()
    client.newCall(req).execute().use { r -> r.code } // 200/204 обычно, тело может быть пустым
}
