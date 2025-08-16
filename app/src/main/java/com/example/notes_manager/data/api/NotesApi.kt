package com.example.notes_manager.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NotesApi {

    // Полноценный ответ (нужен для чтения X-Total-Count и 304)
    @GET("posts")
    suspend fun listNotesResp(
        @Query("_page") page: Int,
        @Query("_limit") limit: Int,
        @Query("q") query: String? = null,
        @Query("_sort") sort: String? = null,    // "id"/"title"
        @Query("_order") order: String? = null   // "asc"/"desc"
    ): Response<List<PostDto>>

    // Вариант "только из кэша", сеть не трогается (если в кэше нет — будет 504 Unsatisfiable Request)
    @GET("posts")
    suspend fun listNotesCacheOnly(
        @Header("Cache-Control") cacheControl: String = "only-if-cached,max-stale=2147483647",
        @Query("_page") page: Int,
        @Query("_limit") limit: Int,
        @Query("q") query: String? = null,
        @Query("_sort") sort: String? = null,
        @Query("_order") order: String? = null
    ): Response<List<PostDto>>
}
