package com.example;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.Embeddable;

@Introspected
@Embeddable
public record Address(String street, String city, String zip) {
}
