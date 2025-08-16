package com.example.notes_manager.data.auth

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun logout()
    val isAuthorized: Flow<Boolean>
}
