package com.example.notes_manager.data

import android.content.Context
import com.example.notes_manager.core.network.RetrofitFactory
import com.example.notes_manager.core.paging.Page
import com.example.notes_manager.data.api.NotesApi
import com.example.notes_manager.data.api.toDomain
import com.example.notes_manager.domain.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlin.math.ceil

/**
 * Репозиторий для "заметок".
 * Есть два режима:
 *  - pageStream(...) — сначала кэш, потом тихо обновляем сетью (для офлайна)
 *  - loadPage(...)   — обычная одноразовая загрузка страницы (для твоего Paginator из MainActivity)
 */
class NotesRepo(context: Context) {

    private val api: NotesApi = RetrofitFactory
        .retrofit(context, "https://jsonplaceholder.typicode.com/")
        .create(NotesApi::class.java)

    /** Оффлайн-режим: сперва кэш (если есть), затем свежие данные из сети. */
    fun pageStream(
        page: Int?,
        limit: Int,
        query: String?,
        sort: String?,
        order: String?
    ): Flow<Page<Int, Post>> = flow {
        val p = page ?: 1

        // 1) Тянем кэш без сети (only-if-cached)
        val cachedResp = withContext(Dispatchers.IO) {
            api.listNotesCacheOnly(
                page = p,
                limit = limit,
                query = query?.ifBlank { null },
                sort = sort,
                order = order
            )
        }
        if (cachedResp.isSuccessful) {
            val cached = cachedResp.body().orEmpty()
            if (cached.isNotEmpty()) {
                emit(Page(cached.map { it.toDomain() }, p + 1))
            }
        }

        // 2) Обновление из сети (ETag/304 обслужит OkHttp-кэш)
        val freshResp = withContext(Dispatchers.IO) {
            api.listNotesResp(
                page = p,
                limit = limit,
                query = query?.ifBlank { null },
                sort = sort,
                order = order
            )
        }

        val freshDtos = freshResp.body().orEmpty()
        val total = freshResp.headers()["X-Total-Count"]?.toIntOrNull()
        val next = when {
            freshDtos.isEmpty() -> null
            total != null -> {
                val maxPage = ceil(total / limit.toDouble()).toInt()
                if (p >= maxPage) null else p + 1
            }
            else -> p + 1
        }

        val cachedIds = cachedResp.body()?.mapNotNull { it.id } ?: emptyList()
        val freshIds = freshDtos.mapNotNull { it.id }
        if (cachedIds.isEmpty() || cachedIds != freshIds) {
            emit(Page(freshDtos.map { it.toDomain() }, next))
        }
    }

    /** Совместимость с твоим Paginator: обычная одноразовая загрузка страницы из сети. */
    suspend fun loadPage(
        page: Int?,
        limit: Int,
        query: String?,
        sort: String?,
        order: String?
    ): Page<Int, Post> = withContext(Dispatchers.IO) {
        val p = page ?: 1

        val resp = api.listNotesResp(
            page = p,
            limit = limit,
            query = query?.ifBlank { null },
            sort = sort,
            order = order
        )

        val dtos = resp.body().orEmpty()
        val total = resp.headers()["X-Total-Count"]?.toIntOrNull()

        val next = when {
            dtos.isEmpty() -> null
            total != null -> {
                val maxPage = ceil(total / limit.toDouble()).toInt()
                if (p >= maxPage) null else p + 1
            }
            else -> p + 1
        }

        Page(dtos.map { it.toDomain() }, next)
    }
}
