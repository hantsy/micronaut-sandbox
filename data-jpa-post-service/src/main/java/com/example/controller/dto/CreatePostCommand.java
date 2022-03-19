package com.example.controller.dto;

import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;

@Introspected
public record CreatePostCommand(@NotBlank String title, @NotBlank String content) {
}
