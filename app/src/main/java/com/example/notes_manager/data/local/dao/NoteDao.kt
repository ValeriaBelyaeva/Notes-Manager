package com.example.notes_manager.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.notes_manager.data.local.entity.*
import com.example.notes_manager.data.local.model.NoteWithTags

@Dao
interface NoteDao {

    @Upsert
    suspend fun upsertNotes(notes: List<NoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Update
    suspend fun update(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Transaction
    @Query("SELECT * FROM notes ORDER BY updatedAtMs DESC")
    fun observeAll(): Flow<List<NoteWithTags>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun observeById(id: Int): Flow<NoteWithTags?>

    // Запрос по имени тега
    @Transaction
    @Query("""
        SELECT n.* FROM notes n
        INNER JOIN note_tag nt ON nt.noteId = n.id
        INNER JOIN tags t ON t.id = nt.tagId
        WHERE t.name LIKE :tagQuery ESCAPE '\'
        ORDER BY n.updatedAtMs DESC
    """)
    fun observeByTag(tagQuery: String): Flow<List<NoteWithTags>>
}
