package com.example.notes_manager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val body: String,
    val createdAtMs: Long,   // epoch millis
    val updatedAtMs: Long    // epoch millis
)
