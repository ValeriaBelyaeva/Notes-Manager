package com.example.notes_manager.domain.usecase

import com.example.notes_manager.domain.error.Outcome
import com.example.notes_manager.domain.model.Note
import com.example.notes_manager.domain.repo.NotesRepository
import kotlinx.coroutines.flow.Flow

class GetNotesUseCase(
    private val repo: NotesRepository
) {
    operator fun invoke(
        page: Int = 1,
        limit: Int = 50,
        query: String? = null
    ): Flow<Outcome<List<Note>>> = repo.observeNotes(page, limit, query)
}
