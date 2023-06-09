package com.example;

import io.micronaut.context.annotation.ConfigurationProperties;

import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(value = "blog")
public record BlogProperties(@NotBlank String title, String description, String author) {
}
