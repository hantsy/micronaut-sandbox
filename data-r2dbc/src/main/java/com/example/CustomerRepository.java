package com.example;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.BiFunction;

@Singleton
@RequiredArgsConstructor
public class CustomerRepository {
    public static final BiFunction<Row, RowMetadata, Customer> MAPPING_FUNCTION = (row, rowMetadata) -> {
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
        var sql = "SELECT *  FROM  customers WHERE id=:id ";
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono
                        .from(connection.createStatement(sql)
                                .bind("id", id)
                                .execute()
                        )
                        .flatMap(r -> Mono.from(r.map(MAPPING_FUNCTION)))
                        .switchIfEmpty(Mono.empty())
                        .doOnTerminate(() -> Mono.from(connection.close()).then())
                );
    }

    Mono<UUID> save(Customer data) {
        var sql = "INSERT INTO customers(name, age, street, city, zip, version) VALUES (:name, :age, :street, :city, :zip, version+1) RETURNING id ";
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono.from(connection.beginTransaction())
                        .then(Mono.from(connection.createStatement(sql)
                                        .bind("name", data.name())
                                        .bind("age", data.age())
                                        .bind("street", data.address().street())
                                        .bind("city", data.address().street())
                                        .bind("zip", data.address().zip())
                                        .returnGeneratedValues("id")
                                        .execute()
                                )
                        )
                        .flatMap(r -> Mono.from(r.map((row, rowMetadata) -> row.get("id", UUID.class))))
                        .switchIfEmpty(Mono.empty())
                        .doOnSuccess((id) -> Mono.from(connection.commitTransaction()).then(Mono.just(id)))
                        .doOnError((r) -> Mono.from(connection.rollbackTransaction()).then())
                        .doOnTerminate(() -> Mono.from(connection.close()).then())
                );
    }

    Mono<Integer> deleteAll() {
        var sql = "DELETE  FROM customers";
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono.from(connection.beginTransaction())
                        .then(Mono.from(connection.createStatement(sql).execute())
                                .flatMap(result -> Mono.from(result.getRowsUpdated()))
                        )
                        .doOnSuccess((rowsUpdated) -> Mono.from(connection.commitTransaction()).then(Mono.just(rowsUpdated)))
                        .doOnError((r) -> Mono.from(connection.rollbackTransaction()).then())
                        .doOnTerminate(() -> Mono.from(connection.close()).then())
                );
    }

    Mono<Integer> deleteById(UUID id) {
        var sql = "DELETE  FROM customers WHERE id=:id";
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono.from(connection.beginTransaction())
                        .then(Mono.from(connection.createStatement(sql)
                                                .bind("id", id)
                                                .execute()
                                        )
                                        .flatMap(result -> Mono.from(result.getRowsUpdated()))
                        )
                        .doOnSuccess((rowsUpdated) -> Mono.from(connection.commitTransaction()).then(Mono.just(rowsUpdated)))
                        .doOnError((r) -> Mono.from(connection.rollbackTransaction()).then())
                        .doOnTerminate(() -> Mono.from(connection.close()).then())
                );
    }
}
