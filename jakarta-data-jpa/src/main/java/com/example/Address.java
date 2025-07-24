package com.example;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.Embeddable;

@Introspected
@Embeddable
@Serdeable
public record Address(
        String street,
        String city,
        String zip) {

    public static Address of(String street, String city, String zip) {
        return new Address(street, city, zip);
    }
}
