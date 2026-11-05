package com.example

import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDateTime
import java.util.UUID

@Serdeable
data class PostDto(
    val id: UUID,
    val title: String,
    val content: String,
    val status: Status?,
    val createdAt: LocalDateTime?
)
