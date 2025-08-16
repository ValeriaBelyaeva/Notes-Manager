package com.example.notes_manager.data.api

import com.example.notes_manager.domain.Post

fun PostDto.toDomain(): Post = Post(
    id = id ?: 0,
    title = title,
    body = body,
    authorId = userId
)
