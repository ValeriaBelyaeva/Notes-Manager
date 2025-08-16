package com.example.notes_manager.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import com.example.notes_manager.core.serialization.InstantIso8601Serializer

@Serializable
data class User(
    val id: Long,
    @SerialName("name") val name: String,
    val email: String? = null,
    @Serializable(with = InstantIso8601Serializer::class)
    val createdAt: Instant
)
