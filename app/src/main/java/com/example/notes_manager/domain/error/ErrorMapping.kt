package com.example.notes_manager.domain.error

import retrofit2.HttpException
import java.io.IOException

fun Throwable.toDomainError(): DomainError = when (this) {
    is IOException -> DomainError.Network
    is HttpException -> when (code()) {
        401 -> DomainError.Unauthorized
        403 -> DomainError.Forbidden
        404 -> DomainError.NotFound
        409 -> DomainError.Conflict
        422 -> DomainError.Unprocessable
        429 -> DomainError.TooManyRequests
        in 500..599 -> DomainError.Server
        else -> DomainError.Unknown(message)
    }
    else -> DomainError.Unknown(message)
}
