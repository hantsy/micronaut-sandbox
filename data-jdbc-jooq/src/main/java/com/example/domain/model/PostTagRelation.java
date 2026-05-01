package com.example.domain.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

import java.util.UUID;

@Introspected
@MappedEntity(value = "posts_tags")
public record PostTagRelation(
        @Id
        UUID id,

        @MappedProperty("post_id")
        UUID postId,

        @MappedProperty("tag_id")
        UUID tagId
) {
    public static PostTagRelation of(UUID postId, UUID tagId) {
        return new PostTagRelation(null, postId, tagId);
    }
}

