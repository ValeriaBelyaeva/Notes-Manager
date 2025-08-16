package com.example.notes_manager.domain.error

sealed class DomainError {
    object Network : DomainError()
    object Unauthorized : DomainError()
    object Forbidden : DomainError()
    object NotFound : DomainError()
    object TooManyRequests : DomainError()
    object Conflict : DomainError()
    object Unprocessable : DomainError()
    object Server : DomainError()
    data class Unknown(val message: String? = null) : DomainError()
}
