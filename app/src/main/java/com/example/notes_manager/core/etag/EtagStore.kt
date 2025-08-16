package com.example.notes_manager.core.etag

import android.content.Context

/** Тупое хранилище ETag по полному URL. Этого достаточно, чтобы условно валидировать GET-запросы. */
class EtagStore(context: Context) {
    private val prefs = context.getSharedPreferences("etag_store", Context.MODE_PRIVATE)

    fun get(url: String): String? = prefs.getString(url, null)

    fun put(url: String, etag: String) {
        prefs.edit().putString(url, etag).apply()
    }
}
