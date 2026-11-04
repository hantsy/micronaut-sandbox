package com.example;

import io.micronaut.data.annotation.Embeddable;

@Embeddable
public record Address(String street, String city, String zip) {
    public static Address of(String street, String city, String zip) {
        return new Address(street, city, zip);
    }
}
