package com.example.domain.repository;

import com.example.domain.model.HashTag;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.UUID;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface HashTagRepository extends CrudRepository<HashTag, UUID> {
}
