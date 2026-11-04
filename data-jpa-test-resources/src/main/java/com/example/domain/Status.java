package com.example.domain;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public enum Status {
    DRAFT, PUBLISHED;
}
