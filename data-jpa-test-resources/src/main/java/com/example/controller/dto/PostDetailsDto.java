package com.example.controller.dto;

import com.example.domain.Status;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostDetailsDto(UUID id, String title, String content, Status status, LocalDateTime createdAt) {
}
