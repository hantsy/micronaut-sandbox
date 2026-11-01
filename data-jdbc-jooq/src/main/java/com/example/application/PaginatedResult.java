package com.example.application;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;

@Serdeable
public record PaginatedResult<T>(List<T> data, Long count) {
}
