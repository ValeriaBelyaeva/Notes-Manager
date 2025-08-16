package com.example.notes_manager.core.util

import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import kotlin.math.min
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Универсальный ретрай с экспоненциальным бэкоффом и небольшим джиттером.
 * По умолчанию ретраит только на IOException и HTTP 5xx/429.
 * Модуль 4 НЕ трогаем: это утилита для других мест (например, пагинация).
 */
suspend fun <T> retrying(
    maxAttempts: Int = 3,
    initialDelayMs: Long = 200,
    factor: Double = 2.0,
    maxDelayMs: Long = 5_000,
    shouldRetry: (Throwable, Int) -> Boolean = ::defaultShouldRetry,
    block: suspend () -> T
): T {
    var delayMs = initialDelayMs
    repeat(maxAttempts - 1) { attemptIdx ->
        try {
            return block()
        } catch (t: Throwable) {
            val attempt = attemptIdx + 1 // номер попытки, на которой упали (1..maxAttempts-1)
            if (!shouldRetry(t, attempt)) throw t
            val jitter = (delayMs * 0.1 * Random.nextDouble()).toLong() // до +10%
            val sleep: Duration = min(delayMs + jitter, maxDelayMs).milliseconds
            delay(sleep)
            delayMs = min((delayMs * factor).toLong(), maxDelayMs)
        }
    }
    return block()
}

/** Ретраим только сетевые ошибки и 429/5xx. */
fun defaultShouldRetry(t: Throwable, attempt: Int): Boolean {
    return when (t) {
        is IOException -> true
        is HttpException -> {
            val c = t.code()
            c == 429 || c in 500..599
        }
        else -> false
    }
}
