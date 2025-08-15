package com.example.notes_manager.core.network

import okhttp3.Headers
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Возвращает миллисекунды до следующей попытки или null, если заголовка нет/не распарсили.
 */
fun parseRetryAfterMillis(headers: Headers, nowMillis: Long = System.currentTimeMillis()): Long? {
    val raw = headers["Retry-After"] ?: return null

    // Вариант 1: число секунд ("5")
    raw.trim().toLongOrNull()?.let { secs ->
        if (secs >= 0) return secs * 1000
    }

    // Вариант 2: HTTP‑date (RFC 1123)
    return try {
        val httpDate = ZonedDateTime.parse(raw, DateTimeFormatter.RFC_1123_DATE_TIME)
        val delta = httpDate.toInstant().toEpochMilli() - nowMillis
        if (delta > 0) delta else 0L
    } catch (_: DateTimeParseException) {
        null
    }
}
