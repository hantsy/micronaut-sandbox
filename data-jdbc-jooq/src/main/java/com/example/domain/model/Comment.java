package com.example.domain.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

import java.time.LocalDateTime;
import java.util.UUID;

@Serdeable
@Introspected
@MappedEntity(value = "comments")
public record Comment(
    @Id @AutoPopulated UUID id,
    String content,

    @MappedProperty("created_at")
    @DateCreated
    LocalDateTime createdAt,
    UUID postId
) {
}
