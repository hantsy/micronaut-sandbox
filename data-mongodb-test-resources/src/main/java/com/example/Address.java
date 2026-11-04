package com.example;

import io.micronaut.data.annotation.Embeddable;
import io.micronaut.serde.annotation.Serdeable;

@Embeddable
@Serdeable
public record Address(String street, String city, String zip) {
    public static Address of(String street, String city, String zip) {
        return new Address(street, city, zip);
    }
}
