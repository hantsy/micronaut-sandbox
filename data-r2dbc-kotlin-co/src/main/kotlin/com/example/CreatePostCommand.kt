package com.example

import io.micronaut.core.annotation.Introspected
import jakarta.validation.constraints.NotBlank

@Introspected
data class CreatePostCommand(
    @field:NotBlank var title: String,
    @field:NotBlank var content: String
)
