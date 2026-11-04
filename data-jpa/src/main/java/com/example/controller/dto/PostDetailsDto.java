package com.example.controller.dto;

import com.example.domain.Status;
import io.micronaut.serde.annotation.Serdeable;

import java.time.LocalDateTime;
import java.util.UUID;

@Serdeable
public record PostDetailsDto(UUID id, String title, String content, Status status, LocalDateTime createdAt) {
}
