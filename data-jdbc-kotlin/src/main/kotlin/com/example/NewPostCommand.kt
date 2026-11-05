package com.example

import io.micronaut.core.annotation.Introspected
import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.NotBlank

@Serdeable
@Introspected
data class NewPostCommand(
    @field:NotBlank var title: String,
    @field:NotBlank var content: String
)
