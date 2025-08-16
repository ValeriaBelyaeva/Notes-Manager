package com.example.notes_manager.core.nb

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Dispatchers

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Loading<T>(val data: T? = null) : Resource<T>()
    data class Error<T>(val throwable: Throwable, val data: T? = null) : Resource<T>()
}

/**
 * Сначала отдаём Room (query), затем при необходимости тянем сеть (fetch) и сохраняем (save),
 * после чего снова эмитим Room. Никаких скачков на экране: сперва кэш, потом обновление.
 */
fun <T> networkBound(
    query: () -> Flow<T>,
    fetch: suspend () -> T,
    save: suspend (T) -> Unit,
    shouldFetch: (T) -> Boolean
): Flow<Resource<T>> = flow {
    val cached = query().first()
    emit(Resource.Success(cached))        // 1) мгновенно показываем, что есть в Room

    if (shouldFetch(cached)) {
        emit(Resource.Loading(cached))    // 2) подсказка UI, что началась сеть
        try {
            val remote = fetch()          // 3) сеть
            save(remote)                  // 4) пишем в Room (транзакцией снаружи или внутри save)
        } catch (t: Throwable) {
            // 5) сеть сломалась — UI остаётся на кэше, но знает про ошибку
            emit(Resource.Error(t, cached))
        }
    }

    // 6) эмитим «свежее из Room» (если save ничего не изменил, данные останутся теми же)
    emitAll(query().map { Resource.Success(it) })
}.flowOn(Dispatchers.IO)
