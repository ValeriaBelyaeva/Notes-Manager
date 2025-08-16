package com.example.notes_manager.core.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json

private val jsonLenient = Json {
    ignoreUnknownKeys = true   // пропускаем лишние поля
    isLenient = true           // допускаем чуть более свободный формат
    encodeDefaults = true
}

fun <T> decodeOrNull(raw: String, deserializer: DeserializationStrategy<T>): T? =
    runCatching { jsonLenient.decodeFromString(deserializer, raw) }.getOrNull()
