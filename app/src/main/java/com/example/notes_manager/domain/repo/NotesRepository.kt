package com.example.notes_manager.domain.repo

import com.example.notes_manager.domain.error.Outcome
import com.example.notes_manager.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun observeNotes(page: Int = 1, limit: Int = 50, query: String? = null): Flow<Outcome<List<Note>>>
}
