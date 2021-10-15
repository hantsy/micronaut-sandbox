package com.example.controller;

import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Introspected
public record CreatePostDto(@NotBlank String title, @NotBlank String content) {
}
