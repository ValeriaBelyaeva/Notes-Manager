package com.example.notes_manager.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface NotesApi {
    @GET("posts")
    suspend fun listNotesResp(
        @Query("_page") page: Int,
        @Query("_limit") limit: Int,
        @Query("q") query: String? = null,
        @Query("_sort") sort: String? = null,
        @Query("_order") order: String? = null
    ): retrofit2.Response<List<PostDto>>
}
