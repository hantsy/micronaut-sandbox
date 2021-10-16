package com.example.repository;

import com.example.domain.Post;
import io.micronaut.data.annotation.*;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.jpa.repository.JpaSpecificationExecutor;
import io.micronaut.data.repository.CrudRepository;

import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> , JpaSpecificationExecutor<Post> {

}