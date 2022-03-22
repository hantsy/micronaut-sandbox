package com.example

import groovy.transform.ToString
import io.micronaut.data.annotation.Embeddable

@Embeddable
@ToString
class Address {
    String street, city, zip

    static Address of(String street, String city, String zip) {
        def address = new Address()
        address.street = street
        address.city = city
        address.zip = zip
        address
    }
}
