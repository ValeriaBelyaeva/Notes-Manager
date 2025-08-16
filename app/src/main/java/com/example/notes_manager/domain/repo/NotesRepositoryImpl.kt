package com.example.notes_manager.data.repo

import android.content.Context
import com.example.notes_manager.core.nb.Resource
import com.example.notes_manager.data.NotesRepository as DataNotesRepo
import com.example.notes_manager.data.mappers.toDomain
import com.example.notes_manager.domain.error.Outcome
import com.example.notes_manager.domain.error.toDomainError
import com.example.notes_manager.domain.model.Note
import com.example.notes_manager.domain.repo.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Адаптер: data.NotesRepository (Room+сеть) → domain интерфейс.
 * Преобразует Resource<List<NoteWithTags>> → Outcome<List<Note>> и маппит ошибки.
 */
class NotesRepositoryImpl(context: Context) : NotesRepository {

    private val dataRepo = DataNotesRepo(context)

    override fun observeNotes(page: Int, limit: Int, query: String?): Flow<Outcome<List<Note>>> {
        return dataRepo.notesStream(page, limit, query)
            .map { res ->
                when (res) {
                    is Resource.Loading -> Outcome.Loading(res.data?.map { it.toDomain() })
                    is Resource.Success -> Outcome.Success(res.data.map { it.toDomain() })
                    is Resource.Error   -> Outcome.Error(res.throwable.toDomainError(), res.data?.map { it.toDomain() })
                }
            }
    }
}
