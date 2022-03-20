package com.example;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Version;

import java.util.UUID;

@Introspected
@MappedEntity(value = "customers")
public record Customer(
        @Id @AutoPopulated UUID id,
        String name,
        Integer age,
        Address address,
        @Version Long version
) {
    public static Customer of(String name, Integer age, Address address) {
        return new Customer(null, name, age, address, null);
    }
}
