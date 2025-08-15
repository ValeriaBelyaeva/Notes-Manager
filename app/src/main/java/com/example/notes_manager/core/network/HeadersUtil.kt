package com.example.notes_manager.core.network

fun headersToPrettyString(headers: Map<String, String>): String =
    headers.entries.joinToString("\n") { (k, v) -> "$k: $v" }
