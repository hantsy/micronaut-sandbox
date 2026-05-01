package com.example.application;

import java.util.List;
import java.util.UUID;

public record PostSummary(UUID id, String title, Long commentsCount, List<String> tags) {
}
