package com.example.controller.dto;

import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;

@Introspected
public record UpdatePostCommand(@NotBlank String title, @NotBlank String content) {
}
