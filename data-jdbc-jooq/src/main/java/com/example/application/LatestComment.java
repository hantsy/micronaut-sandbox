package com.example.application;

import io.micronaut.serde.annotation.Serdeable;
import java.time.LocalDateTime;

@Serdeable
public record LatestComment(String content, LocalDateTime createdAt) {
}
