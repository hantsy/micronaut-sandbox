package com.example.dto;

import com.example.Address;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record CustomerDto(
        String id,
        String name,
        Integer age,
        Address address
) {}
