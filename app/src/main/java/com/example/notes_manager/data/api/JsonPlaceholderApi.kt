package com.example.notes_manager.data.api

import retrofit2.http.*

interface JsonPlaceholderApi {
    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: Int): PostDto

    @POST("posts")
    suspend fun createPost(@Body req: NewPostRequest): PostDto
}
