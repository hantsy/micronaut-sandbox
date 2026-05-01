package com.example.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostDetails(UUID id,
                          String title,
                          String content,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt,
                          Long commentsCount, LatestComment latestComment) {
}
