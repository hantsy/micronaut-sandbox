package com.example.domain.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.annotation.Version;

import java.time.LocalDateTime;
import java.util.UUID;

@Introspected
@MappedEntity(value = "posts")
public record Post(
    @Id UUID id,
    String title,
    String content,
    Status status,
    @MappedProperty("created_at")
    @DateCreated
    LocalDateTime createdAt,
    @MappedProperty("updated_at")
    @DateUpdated
    LocalDateTime updatedAt,
    @Version Long version
) {
    // Static factory method
    public static Post of(String title, String content) {
        return new Post(null, title, content, null, null, null, null);
    }
}
