package com.example;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;

import static io.micronaut.data.annotation.Relation.Kind.EMBEDDED;

@MappedEntity("customers")
public record Customer(
    @Id @GeneratedValue String id,
    String name,
    Integer age,
    @Relation(EMBEDDED) Address address
) {
    public static Customer of(String name, Integer age, Address address) {
        return new Customer(null, name, age, address);
    }
}
