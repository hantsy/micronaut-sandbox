package com.example;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.BiFunction;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class CustomerRepositoryWithConnectionFactory {
    public static final BiFunction<Row, RowMetadata, Customer> MAPPING_FUNCTION = (row, rowMetadata) -> {
        log.debug("row: {}, metadata: {}", row, rowMetadata);
        var id = row.get("id", UUID.class);
        var name = row.get("name", String.class);
        var age = row.get("age", Integer.class);
        var street = row.get("street", String.class);
        var city = row.get("city", String.class);
        var zip = row.get("zip", String.class);
        var version = row.get("version", Long.class);
        return new Customer(id, name, age, new Address(street, city, zip), version);
    };
    private final ConnectionFactory connectionFactory;

    Flux<Customer> findAll() {
        var sql = "SELECT *  FROM  customers ";
        return Mono.from(connectionFactory.create())
                .flatMapMany(connection -> Flux
                        .from(connection.createStatement(sql).execute())
                        .flatMap(r -> r.map(MAPPING_FUNCTION))
                        .doOnTerminate(() -> Mono.from(connection.close()).then())
                );
    }

    Mono<Customer> findById(UUID id) {
        var sql = "SELECT * FROM customers WHERE id=$1 ";
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono
                        .from(connection.createStatement(sql)
                                .bind(0, id)
                                .execute()
                        )
                        .flatMap(r -> Mono.from(r.map(MAPPING_FUNCTION)))
                        .switchIfEmpty(Mono.empty())
                        .doOnTerminate(() -> Mono.from(connection.close()).then())
                );
    }

    Mono<UUID> save(Customer data) {
        var sql = "INSERT INTO customers (name, age, street, city, zip) VALUES ($1, $2, $3, $4, $5)";
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono
                        .from(connection.createStatement(sql)
                                .bind(0, data.name())
                                .bind(1, data.age())
                                .bind(2, data.address().street())
                                .bind(3, data.address().street())
                                .bind(4, data.address().zip())
                                .returnGeneratedValues("id")
                                .execute()

                        )
                        .flatMap(r -> Mono.from(r.map((row, rowMetadata) -> row.get("id", UUID.class))))
                        .switchIfEmpty(Mono.empty())
                        .doOnTerminate(() -> Mono.from(connection.close()).then())
                );
    }

    Mono<Integer> deleteAll() {
        var sql = "DELETE FROM customers";
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono
                        .from(connection.createStatement(sql).execute())
                        .flatMap(result -> Mono.from(result.getRowsUpdated()))
                        .doOnTerminate(() -> Mono.from(connection.close()).then())
                );
    }

    Mono<Integer> deleteById(UUID id) {
        var sql = "DELETE  FROM customers WHERE id=$1";
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono
                        .from(connection.createStatement(sql)
                                .bind(0, id)
                                .execute()
                        )
                        .flatMap(result -> Mono.from(result.getRowsUpdated()))
                        .doOnTerminate(() -> Mono.from(connection.close()).then())
                );
    }
}
