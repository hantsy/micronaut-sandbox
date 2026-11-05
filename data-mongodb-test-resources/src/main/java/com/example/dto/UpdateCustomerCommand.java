package com.example.dto;

import com.example.Address;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Introspected
@Serdeable
public record UpdateCustomerCommand(
        @NotBlank String name,
        @NotNull Integer age,
        @NotNull Address address
) {}
