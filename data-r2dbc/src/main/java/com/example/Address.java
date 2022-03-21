package com.example;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.Embeddable;
import io.micronaut.data.annotation.MappedProperty;

@Introspected
@Embeddable
public record Address(
        @MappedProperty("street") String street,
        @MappedProperty("city") String city,
        @MappedProperty("zip") String zip
) {

    public static Address of(String street, String city, String zip) {
        return new Address(street, city, zip);
    }
}
