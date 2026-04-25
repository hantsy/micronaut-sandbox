package com.example;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.annotation.Version;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static io.micronaut.data.annotation.Relation.Kind.EMBEDDED;

@Introspected
@MappedEntity(value = "customers")
public record Customer(
        @Id @AutoPopulated UUID id,
        String name,
        Integer age,
        @Relation(EMBEDDED) Address address,
        @Version Long version
) {
    public static Customer of(String name, Integer age, Address address) {
        return new Customer(null, name, age, address, null);
    }
}

record CustomerOrderSummary(
        UUID id,
        String name,
        Integer ordersCount
) {
}

record OrderDetails(
        UUID orderId,
        BigDecimal amount,
        String customerName,
        List<OrderItemDetails> items
) {
}

record OrderItemDetails(
        String productName,
        BigDecimal price,
        Integer quantity
) {
}

