package com.example;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CustomCustomerRepository {
    Flux<Customer> findAll();

    Mono<Customer> findById(UUID id);

    Mono<UUID> save(Customer data);

    Mono<Long> deleteAll();

    Mono<Long> deleteById(UUID id);
}
