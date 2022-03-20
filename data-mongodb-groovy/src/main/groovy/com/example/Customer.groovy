package com.example

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity

@MappedEntity("customers")
class Customer {

    @Id
    @GeneratedValue
    String id
    String name
    Integer age
    Address address

    static Customer of(String name, Integer age, Address address) {
        def customer = new Customer()
        customer.name = name
        customer.age = age
        customer.address = address
        customer
    }
}
