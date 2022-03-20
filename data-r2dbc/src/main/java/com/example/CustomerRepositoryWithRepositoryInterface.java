package com.example;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;

import java.util.UUID;

@Repository()
@R2dbcRepository(dialect = Dialect.POSTGRES)
public abstract class CustomerRepositoryWithRepositoryInterface implements ReactorCrudRepository<Customer, UUID> {

}
