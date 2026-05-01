package com.example.domain.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

import java.util.UUID;

@Introspected
@MappedEntity(value = "hash_tags")
public record HashTag(
    @Id UUID id,
    String name
) {
}
