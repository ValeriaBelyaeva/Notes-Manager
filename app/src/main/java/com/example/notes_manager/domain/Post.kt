package com.example.notes_manager.domain

data class Post(
    val id: Int,
    val title: String,
    val body: String,
    val authorId: Int?
)
