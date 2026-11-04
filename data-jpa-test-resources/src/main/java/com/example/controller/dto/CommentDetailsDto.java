package com.example.controller.dto;

import java.time.LocalDateTime;
import io.micronaut.serde.annotation.Serdeable;
import java.util.UUID;

@Serdeable
public record CommentDetailsDto(UUID id, String content, LocalDateTime createdAt) {
}
