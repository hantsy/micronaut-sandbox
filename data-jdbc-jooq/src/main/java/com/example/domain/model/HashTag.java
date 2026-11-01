package com.example.domain.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

import java.util.UUID;

@Serdeable
@Introspected
@MappedEntity(value = "hash_tags")
public record HashTag(
    @Id @AutoPopulated UUID id,
    String name
) {
}
