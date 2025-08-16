package com.example.notes_manager.data.api

import kotlinx.serialization.Serializable

@Serializable
data class PostDto(
    val userId: Int? = null,
    val id: Int? = null,
    val title: String,
    val body: String
)

@Serializable
data class NewPostRequest(
    val userId: Int,
    val title: String,
    val body: String
)
