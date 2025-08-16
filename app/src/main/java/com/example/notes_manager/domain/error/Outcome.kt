package com.example.notes_manager.domain.error

sealed class Outcome<out T> {
    data class Success<T>(val data: T) : Outcome<T>()
    data class Loading<T>(val data: T? = null) : Outcome<T>()
    data class Error<T>(val error: DomainError, val data: T? = null) : Outcome<T>()
}
