package com.example.notes_manager.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NotesApi {
    // Удобная версия, когда не нужен total-count
    @GET("posts")
    suspend fun listNotes(
        @Query("_page") page: Int,
        @Query("_limit") limit: Int,
        @Query("q") query: String? = null,
        @Query("_sort") sort: String? = null,   // "id" / "title"
        @Query("_order") order: String? = null  // "asc" / "desc"
    ): List<PostDto>

    // Версия с полной обёрткой Response — нужна для чтения заголовков (X-Total-Count)
    @GET("posts")
    suspend fun listNotesResp(
        @Query("_page") page: Int,
        @Query("_limit") limit: Int,
        @Query("q") query: String? = null,
        @Query("_sort") sort: String? = null,
        @Query("_order") order: String? = null
    ): Response<List<PostDto>>
}
