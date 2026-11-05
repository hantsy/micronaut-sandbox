package com.example.customers.dto;

import com.example.customers.Address;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record CustomerDto(
        String id,
        String name,
        Integer age,
        Address address
) {}
