package com.example.controller.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentDetailsDto(UUID id, String content, LocalDateTime createdAt) {
}
