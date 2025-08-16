package com.example.notes_manager.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface JsonPlaceholderApi {
    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: Int): PostDto

    @POST("posts")
    suspend fun createPost(@Body req: NewPostRequest): PostDto
}
