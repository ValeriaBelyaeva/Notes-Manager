package com.example.notes_manager.data.local.dao

import androidx.room.*
import com.example.notes_manager.data.local.entity.TagEntity

@Dao
interface TagDao {
    @Upsert
    suspend fun upsertAll(tags: List<TagEntity>): List<Long>

    @Upsert
    suspend fun upsert(tag: TagEntity): Long

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): TagEntity?
}
