package com.example.controller;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostSummaryDto(UUID id, String title, LocalDateTime createdAt) {
}
