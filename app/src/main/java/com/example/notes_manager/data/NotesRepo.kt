package com.example.notes_manager.data

import android.content.Context
import com.example.notes_manager.core.network.RetrofitFactory
import com.example.notes_manager.core.paging.Page
import com.example.notes_manager.data.api.NotesApi
import com.example.notes_manager.data.api.toDomain
import com.example.notes_manager.domain.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.notes_manager.core.util.retrying
import com.example.notes_manager.core.util.defaultShouldRetry
import kotlin.math.ceil

class NotesRepo(context: Context) {

    private val api: NotesApi = RetrofitFactory
        .retrofit(context, "https://jsonplaceholder.typicode.com/")
        .create(NotesApi::class.java)

    suspend fun loadPage(
        page: Int?,
        limit: Int,
        query: String?,
        sort: String?,
        order: String?
    ): Page<Int, Post> = withContext(Dispatchers.IO) {
        val p = page ?: 1

        // Ретраим только сетевые/5xx/429. Модуль 4 не трогаем.
        val resp = retrying(
            maxAttempts = 3,
            initialDelayMs = 200,
            factor = 2.0,
            shouldRetry = ::defaultShouldRetry
        ) {
            api.listNotesResp(
                page = p,
                limit = limit,
                query = query?.ifBlank { null },
                sort = sort,
                order = order
            )
        }

        val dtos = resp.body().orEmpty()
        val totalCount = resp.headers()["X-Total-Count"]?.toIntOrNull()

        val next = when {
            dtos.isEmpty() -> null
            totalCount != null -> {
                val maxPage = ceil(totalCount / limit.toDouble()).toInt()
                if (p >= maxPage) null else p + 1
            }
            else -> p + 1
        }

        Page(
            items = dtos.map { it.toDomain() },
            nextKey = next
        )
    }
}
