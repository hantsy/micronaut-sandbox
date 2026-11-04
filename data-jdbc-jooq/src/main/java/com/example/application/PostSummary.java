package com.example.application;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import java.util.UUID;

@Serdeable
public record PostSummary(UUID id, String title, Long commentsCount, List<String> tags) {
}
