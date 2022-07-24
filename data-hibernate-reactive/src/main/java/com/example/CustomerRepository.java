package com.example;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.reactive.ReactorPageableRepository;

import java.util.UUID;

@Repository
public interface CustomerRepository extends ReactorPageableRepository<Customer, UUID> {
}

