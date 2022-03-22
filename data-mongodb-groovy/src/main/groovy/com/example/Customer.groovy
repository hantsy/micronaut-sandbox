package com.example

import groovy.transform.ToString
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.Relation

import static io.micronaut.data.annotation.Relation.Kind.EMBEDDED

@MappedEntity("customers")
@ToString
class Customer {

    @Id
    @GeneratedValue
    String id
    String name
    Integer age
    @Relation(EMBEDDED) Address address

    static Customer of(String name, Integer age, Address address) {
        def customer = new Customer()
        customer.name = name
        customer.age = age
        customer.address = address
        customer
    }
}
