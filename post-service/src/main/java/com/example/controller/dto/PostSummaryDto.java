package com.example.controller.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostSummaryDto(UUID id, String title, LocalDateTime createdAt) {
}
