package com.example.controller;

import javax.validation.constraints.NotNull;

public record CreateCommentDto(@NotNull String content) {
}
