package com.example.notes_manager.data.mappers

import com.example.notes_manager.data.local.model.NoteWithTags
import com.example.notes_manager.domain.model.Note

fun NoteWithTags.toDomain(): Note = Note(
    id = note.id,
    title = note.title,
    body = note.body,
    tags = tags.map { it.name }
)
