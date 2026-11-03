package com.example;

import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;

import java.util.UUID;

@R2dbcRepository(dialect = Dialect.POSTGRES)
public abstract class CustomerRepository implements ReactorCrudRepository<Customer, UUID> {

}
