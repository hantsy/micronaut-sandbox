package com.example.customers.dto;

import com.example.customers.Address;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Serdeable
public record NewCustomerCommand(
        @NotBlank String name,
        @NotNull Integer age,
        Address address
) {}
