package com.example.application;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import java.util.UUID;

@Serdeable
public record CreatePostCommand(String title, String content, List<UUID> tagId) {
}
