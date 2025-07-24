package com.example;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;

import java.util.List;
import java.util.UUID;


@Repository
@JdbcRepository(dialect = Dialect.POSTGRES)
public interface CustomerRepository extends CrudRepository<Customer, UUID> {

    Page<Customer> findByAddressCityLike(String cityLike, PageRequest pageRequest);
}
