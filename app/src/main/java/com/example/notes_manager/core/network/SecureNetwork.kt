package com.example.notes_manager.core.network

import android.content.Context
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient

/**
 * Клиент с Certificate Pinning под конкретный хост.
 * Пины — это SHA-256 от публичного ключа (SPKI). Дай минимум 2 пина: текущий и запасной.
 */
object SecureNetwork {

    fun client(
        context: Context,
        host: String,
        pins: List<String>
    ): OkHttpClient {
        val pinner = CertificatePinner.Builder()
            .apply { pins.forEach { add(host, it) } }
            .build()

        // Берём твой базовый клиент и навешиваем пиннер.
        return Network.client(context)
            .newBuilder()
            .certificatePinner(pinner)
            .build()
    }
}
