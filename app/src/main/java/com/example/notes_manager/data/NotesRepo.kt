package com.example.notes_manager.data

import android.content.Context
import com.example.notes_manager.core.network.RetrofitFactory
import com.example.notes_manager.core.paging.Page
import com.example.notes_manager.data.api.NotesApi
import com.example.notes_manager.data.api.toDomain
import com.example.notes_manager.domain.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil

/**
 * Репозиторий для постраничной загрузки «заметок» (posts) с jsonplaceholder.
 * Поддерживает пагинацию, поиск (q), сортировку (_sort/_order).
 */
class NotesRepo(context: Context) {

    private val api: NotesApi = RetrofitFactory
        .retrofit(context, "https://jsonplaceholder.typicode.com/")
        .create(NotesApi::class.java)

    /**
     * Загружает одну страницу.
     * @param page null = первая страница (1)
     * @param limit размер страницы
     * @param query строка поиска (или null)
     * @param sort поле сортировки ("id" / "title" и т.п.)
     * @param order порядок ("asc" / "desc")
     * @return Page<Int, Post> с nextKey = номер следующей страницы или null, если данных больше нет
     */
    suspend fun loadPage(
        page: Int?,
        limit: Int,
        query: String?,
        sort: String?,
        order: String?
    ): Page<Int, Post> = withContext(Dispatchers.IO) {
        val p = page ?: 1

        // Берём полный Response, чтобы вытащить X-Total-Count
        val resp = api.listNotesResp(
            page = p,
            limit = limit,
            query = query?.ifBlank { null },
            sort = sort,
            order = order
        )

        val dtos = resp.body().orEmpty()
        val totalCount = resp.headers()["X-Total-Count"]?.toIntOrNull()

        val next = when {
            dtos.isEmpty() -> null
            totalCount != null -> {
                val maxPage = ceil(totalCount / limit.toDouble()).toInt()
                if (p >= maxPage) null else p + 1
            }
            else -> p + 1 // если сервер не дал total, считаем что следующая есть
        }

        Page(
            items = dtos.map { it.toDomain() },
            nextKey = next
        )
    }
}
