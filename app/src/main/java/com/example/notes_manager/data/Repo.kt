package com.example.notes_manager.data

import android.content.Context
import com.example.notes_manager.core.network.RetrofitFactory
import com.example.notes_manager.data.api.JsonPlaceholderApi
import com.example.notes_manager.data.api.NewPostRequest
import com.example.notes_manager.data.api.PostDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

sealed class NetResult<out T> {
    data class Ok<T>(val data: T): NetResult<T>()
    data class Err(val code: Int? = null, val message: String): NetResult<Nothing>()
}

class Repo(context: Context) {
    private val api: JsonPlaceholderApi =
        RetrofitFactory.retrofit(context, "https://jsonplaceholder.typicode.com/")
            .create(JsonPlaceholderApi::class.java)

    suspend fun loadPost(id: Int): NetResult<PostDto> = safe {
        api.getPost(id)
    }

    suspend fun createPost(userId: Int, title: String, body: String): NetResult<PostDto> = safe {
        api.createPost(NewPostRequest(userId, title, body))
    }

    private suspend inline fun <T> safe(crossinline block: suspend () -> T): NetResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                NetResult.Ok(block())
            } catch (e: HttpException) {
                NetResult.Err(e.code(), "HTTP ${e.code()}: ${e.message()}")
            } catch (e: IOException) {
                NetResult.Err(null, "I/O: ${e.message}")
            } catch (e: Throwable) {
                NetResult.Err(null, "Unexpected: ${e.message}")
            }
        }
    }
}
