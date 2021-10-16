package com.example.controller.dto;

import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Introspected
public record CreateCommentCommand(@NotBlank @Size(min = 5, max = 200) String content) {
}
