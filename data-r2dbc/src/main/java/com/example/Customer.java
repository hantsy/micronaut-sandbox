package com.example;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.data.annotation.*;

import java.util.UUID;

import static io.micronaut.data.annotation.Relation.Kind.EMBEDDED;

@Introspected
@MappedEntity(value = "customers")
@Serdeable
public record Customer(
        @Id @AutoPopulated @MappedProperty(definition = "UUID DEFAULT gen_random_uuid()") UUID id,
        String name,
        Integer age,
        @Relation(EMBEDDED) Address address,
        @Version @MappedProperty(definition = "BIGINT DEFAULT 0") Long version
) {
    public static Customer of(String name, Integer age, Address address) {
        return new Customer(null, name, age, address, null);
    }
}
