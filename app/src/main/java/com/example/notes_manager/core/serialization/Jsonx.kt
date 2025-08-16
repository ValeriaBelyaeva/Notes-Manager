package com.example.notes_manager.core.serialization

import kotlinx.serialization.json.Json

/** Единый Json-конфиг для Retrofit/парсинга. */
val JsonX: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
}
