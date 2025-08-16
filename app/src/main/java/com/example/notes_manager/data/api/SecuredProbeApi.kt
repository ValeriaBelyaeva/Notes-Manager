package com.example.notes_manager.data.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface SecuredProbeApi {
    @GET
    suspend fun get(@Url url: String): ResponseBody
}
