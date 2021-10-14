package com.example.controller;

import javax.validation.constraints.NotNull;

public record CreatePostDto(@NotNull String title, @NotNull String content) {
}
