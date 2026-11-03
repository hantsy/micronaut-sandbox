package com.example;

import io.micronaut.transaction.reactive.ReactiveTransactionOperations;
import io.r2dbc.spi.Connection;
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
public class CustomerDaoWithTxOperations implements CustomerDao {
    public static final BiFunction<Row, RowMetadata, Customer> MAPPING_FUNCTION = (row, rowMetadata) -> {
        log.debug("row: {}, metadata: {}", row, rowMetadata);
        var id = row.get("id", UUID.class);
        var name = row.get("name", String.class);
        var age = row.get("age", Integer.class);
        var street = row.get("street", String.class);
        var city = row.get("city", String.class);
        var zip = row.get("zip", String.class);
        var version = row.get("age", Long.class);
        return new Customer(id, name, age, new Address(street, city, zip), version);
    };
    private final ReactiveTransactionOperations<Connection> txOperations;

    @Override
    public Flux<Customer> findAll() {
        var sql = "SELECT *  FROM  customers ";
        return Flux.from(
                txOperations.withTransaction(status ->
                        Mono.just(status.getConnection())
                                .flatMapMany(connection ->
                                        Flux.from(connection.createStatement(sql).execute())
                                                .flatMap(r -> r.map(MAPPING_FUNCTION))
                                )
                )
        );
    }

    @Override
    public Mono<Customer> findById(UUID id) {
        var sql = "SELECT *  FROM  customers WHERE id=$1 ";
        return Mono.from(
                txOperations.withTransaction(status ->
                        Mono.just(status.getConnection())
                                .flatMap(connection ->
                                        Mono.from(connection.createStatement(sql)
                                                        .bind(0, id)
                                                        .execute()
                                                )
                                                .flatMap(r -> Mono.from(r.map(MAPPING_FUNCTION)))
                                                .switchIfEmpty(Mono.empty())
                                )
                )
        );
    }

    @Override
    public Mono<UUID> save(Customer data) {
        //var sql = "INSERT INTO customers(name, age, street, city, zip) VALUES (?, ?, ?, ?, ?) RETURNING id ";
        var sql = "INSERT INTO customers (name, age, street, city, zip) VALUES ($1, $2, $3, $4, $5)";
        return Mono.from(
                txOperations.withTransaction(status ->
                        Mono.just(status.getConnection())
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
                                )

                )
        );
    }

    @Override
    public Mono<Long> deleteAll() {
        var sql = "DELETE  FROM customers";
        return Mono.from(
                txOperations.withTransaction(status ->
                        Mono.just(status.getConnection())
                                .flatMap(connection -> Mono
                                        .from(connection.createStatement(sql).execute())
                                        .flatMap(result -> Mono.from(result.getRowsUpdated()))
                                )
                )
        );
    }

    @Override
    public Mono<Long> deleteById(UUID id) {
        var sql = "DELETE FROM customers WHERE id=$1";
        return Mono.from(
                txOperations.withTransaction(status ->
                        Mono.just(status.getConnection())
                                .flatMap(connection -> Mono
                                        .from(connection.createStatement(sql)
                                                .bind(0, id)
                                                .execute()
                                        )
                                        .flatMap(result -> Mono.from(result.getRowsUpdated()))
                                )
                )
        );
    }
}
