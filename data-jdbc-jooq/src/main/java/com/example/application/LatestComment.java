package com.example.application;

import java.time.LocalDateTime;

public record LatestComment(String content, LocalDateTime createdAt) {
}
