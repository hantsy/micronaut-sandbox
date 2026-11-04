package com.example.controller.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

import jakarta.validation.constraints.NotBlank;

@Introspected
@Serdeable
public record UpdatePostCommand(@NotBlank String title, @NotBlank String content) {
}
