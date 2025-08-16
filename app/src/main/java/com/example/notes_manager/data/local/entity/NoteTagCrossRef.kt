package com.example.notes_manager.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "note_tag",
    primaryKeys = ["noteId", "tagId"]
)
data class NoteTagCrossRef(
    val noteId: Int,
    val tagId: Int
)
