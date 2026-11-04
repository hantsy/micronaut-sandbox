package com.example.customers;

import lombok.AllArgsConstructor;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Serdeable
public class Address {
    private String street;
    private String city;
    private String zip;
}
