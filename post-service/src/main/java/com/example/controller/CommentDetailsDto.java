package com.example.controller;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public record CommentDetailsDto(UUID id, @NotNull String content) {
}
