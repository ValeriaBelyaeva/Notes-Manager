package com.example.notes_manager.data

import android.content.Context
import com.example.notes_manager.core.network.RetrofitFactory
import com.example.notes_manager.core.paging.Page
import com.example.notes_manager.data.api.NotesApi
import com.example.notes_manager.data.api.toDomain
import com.example.notes_manager.domain.Post

class NotesRepo(context: Context) {
    private val api = RetrofitFactory
        .retrofit(context, "https://jsonplaceholder.typicode.com/")
        .create(NotesApi::class.java)

    suspend fun loadPage(
        page: Int?,
        limit: Int,
        query: String?,
        sort: String?,
        order: String?
    ): Page<Int, Post> {
        val p = page ?: 1
        val dtos = api.listNotes(
            page = p,
            limit = limit,
            query = query?.ifBlank { null },
            sort = sort,
            order = order
        )
        val next = if (dtos.isEmpty()) null else p + 1
        return Page(dtos.map { it.toDomain() }, next)
    }
}
