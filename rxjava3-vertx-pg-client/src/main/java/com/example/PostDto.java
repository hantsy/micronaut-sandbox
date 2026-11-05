package com.example;

import io.micronaut.serde.annotation.Serdeable;

import java.time.LocalDateTime;
import java.util.UUID;

@Serdeable
public record PostDto(UUID id, String title, String content, LocalDateTime createdAt) {}
