package com.example;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Introspected
@Serdeable.Deserializable
public record CreatePostCommand(@NotBlank String title, @NotBlank String content) {
}
