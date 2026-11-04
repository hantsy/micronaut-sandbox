package com.example.application;

import io.micronaut.serde.annotation.Serdeable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Serdeable
public record PostDetails(UUID id,
                          String title,
                          String content,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt,
                          Long commentsCount, LatestComment latestComment) {
}
