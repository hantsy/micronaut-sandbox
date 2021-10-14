package com.example.controller;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostDetailsDto(UUID id, String title, String content, LocalDateTime createdAt) {
}
