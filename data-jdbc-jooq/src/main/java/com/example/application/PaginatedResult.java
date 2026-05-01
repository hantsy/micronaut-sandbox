package com.example.application;

import java.util.List;

public record PaginatedResult<T>(List<T> data, Long count) {
}
