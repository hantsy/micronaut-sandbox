package com.example.domain.repository;

import com.example.domain.model.Post;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface PostRepository extends CrudRepository<Post, UUID> {

    @Query("SELECT * FROM posts where title like :title")
    public List<Post> findByTitleContains(String title);
}

