package com.example;

import io.micronaut.context.annotation.ConfigurationProperties;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties(value = "blog")
public record BlogProperties(@NotBlank String title, String description, String author) {
}
