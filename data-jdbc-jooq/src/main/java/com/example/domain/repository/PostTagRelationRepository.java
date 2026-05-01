package com.example.domain.repository;

import com.example.domain.model.PostTagRelation;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.UUID;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface PostTagRelationRepository extends CrudRepository<PostTagRelation, UUID> {
}
