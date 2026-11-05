package com.example.dto;

import com.example.Address;
import io.micronaut.serde.annotation.Serdeable;

import java.util.UUID;

@Serdeable
public record CustomerDto(
        UUID id,
        String name,
        Integer age,
        Address address
) {}
