package com.example.controller.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Introspected
@Serdeable
public record CreateCommentCommand(@NotBlank @Size(min = 5, max = 200) String content) {
}
