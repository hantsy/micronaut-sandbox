package com.example.dto

import com.example.Address
import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Serdeable
class NewCustomerCommand {
    @NotBlank
    String name
    @NotNull
    Integer age
    Address address
}
