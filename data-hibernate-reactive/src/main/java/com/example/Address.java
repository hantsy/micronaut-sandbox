package com.example;

import lombok.*;
import io.micronaut.serde.annotation.Serdeable;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Serdeable
public class Address implements Serializable {
    private String street;
    private String city;
    private String zip;

    static Address of(String street, String city, String zip) {
        Address address = new Address();
        address.street = street;
        address.city = city;
        address.zip = zip;
        return address;
    }
}
