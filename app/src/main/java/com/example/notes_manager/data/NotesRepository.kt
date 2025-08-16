package com.example.notes_manager.data

import android.content.Context
import androidx.room.withTransaction
import com.example.notes_manager.core.db.AppDatabase
import com.example.notes_manager.core.nb.Resource
import com.example.notes_manager.core.nb.networkBound
import com.example.notes_manager.core.network.RetrofitFactory
import com.example.notes_manager.data.api.NotesApi
import com.example.notes_manager.data.local.entity.NoteEntity
import com.example.notes_manager.data.local.model.NoteWithTags
import kotlinx.coroutines.flow.Flow

class NotesRepository(context: Context) {

    private val db = AppDatabase.get(context)

    private val notesApi: NotesApi = RetrofitFactory
        .retrofit(context, "https://jsonplaceholder.typicode.com/")
        .create(NotesApi::class.java)

    /**
     * Сначала Room (Flow<List<NoteWithTags>>), затем сеть и обновление Room.
     * В fetch возвращаем ту же локальную модель: NoteWithTags с пустыми тегами.
     */
    fun notesStream(
        page: Int = 1,
        limit: Int = 50,
        query: String? = null
    ): Flow<Resource<List<NoteWithTags>>> {

        val noteDao = db.noteDao()

        return networkBound(
            // 1) локальный кэш
            query = { noteDao.observeAll() },

            // 2) сеть -> локальная модель (NoteWithTags), теги пустые
            fetch = {
                val resp = notesApi.listNotesResp(
                    page = page,
                    limit = limit,
                    query = query,
                    sort = "id",
                    order = "asc"
                )
                val now = System.currentTimeMillis()
                val dtos = resp.body().orEmpty()

                dtos.map { dto ->
                    val note = NoteEntity(
                        id = dto.id ?: 0,
                        title = dto.title,
                        body = dto.body,
                        createdAtMs = now,
                        updatedAtMs = now
                    )
                    NoteWithTags(note = note, tags = emptyList())
                }
            },

            // 3) сохраняем в Room только заметки; теги не трогаем
            save = { list ->
                db.withTransaction {
                    val notes = list.map { it.note }
                    db.noteDao().upsertNotes(notes)   // suspend внутри suspend-транзакции
                }
            },

            // 4) критерий обновления
            shouldFetch = { cached -> cached.isEmpty() }
        )
    }

    // Поиск по тегу из Room
    fun notesByTag(tag: String) = db.noteDao().observeByTag(
        "%" + tag.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%"
    )

    // Установка тегов локально (тоже нужна suspend-транзакция)
    suspend fun setTags(noteId: Int, names: List<String>) {
        db.withTransaction {
            db.noteTagDao().setTagsForNote(noteId, names, db.tagDao())
        }
    }
}
