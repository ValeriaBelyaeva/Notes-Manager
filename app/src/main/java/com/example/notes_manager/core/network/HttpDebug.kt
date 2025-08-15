package com.example.notes_manager.core.network

data class HttpDebug(
    val code: Int,
    val headers: Map<String, String>,
    val bodyFirst200: String
)
