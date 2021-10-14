package com.example.repository;

import com.example.domain.Comment;
import com.example.domain.Post;
import io.micronaut.data.annotation.*;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByPost(Post post);
}