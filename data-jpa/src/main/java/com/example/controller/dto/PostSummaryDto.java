package com.example.controller.dto;

import java.time.LocalDateTime;
import io.micronaut.serde.annotation.Serdeable;
import java.util.UUID;

@Serdeable
public record PostSummaryDto(UUID id, String title, LocalDateTime createdAt) {
}
