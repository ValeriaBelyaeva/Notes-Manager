package com.example.notes_manager.domain.model

data class Note(
    val id: Int,
    val title: String,
    val body: String,
    val tags: List<String>
)
