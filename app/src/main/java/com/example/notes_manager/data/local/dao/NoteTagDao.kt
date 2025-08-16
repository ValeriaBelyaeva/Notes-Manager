package com.example.notes_manager.data.local.dao

import androidx.room.*
import com.example.notes_manager.data.local.entity.*

@Dao
interface NoteTagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLink(link: NoteTagCrossRef)

    @Query("DELETE FROM note_tag WHERE noteId = :noteId")
    suspend fun clearTagsForNote(noteId: Int)

    @Transaction
    suspend fun setTagsForNote(noteId: Int, tagNames: List<String>, tagDao: TagDao) {
        clearTagsForNote(noteId)
        val tagIds = tagNames.distinct().map { name ->
            val existing = tagDao.findByName(name)
            val id = existing?.id ?: run {
                val newId = tagDao.upsert(TagEntity(name = name))
                // upsert вернёт rowId, но с авто-генерацией room вернёт id только при insert.
                // На практике проще перечитать:
                tagDao.findByName(name)?.id ?: error("Tag insert failed for '$name'")
            }
            id
        }
        tagIds.forEach { insertLink(NoteTagCrossRef(noteId = noteId, tagId = it)) }
    }
}
