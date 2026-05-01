package com.example.application.util;

import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;

import static com.example.demo.jooq.Tables.COMMENTS;
import static com.example.demo.jooq.Tables.HASH_TAGS;
import static com.example.demo.jooq.Tables.POSTS;
import static com.example.demo.jooq.Tables.POSTS_TAGS;

@Singleton
@Transactional
@Requires(notEnv = "mock")
@RequiredArgsConstructor
@Slf4j
public class SampleDataInitializer {
    private final DSLContext dslContext;

    @EventListener()
    public void init(ServerStartupEvent event) {
        log.debug("start data initialization...");
        var tagJooq = dslContext.insertInto(HASH_TAGS)
                .columns(HASH_TAGS.NAME)
                .values("jooq")
                .returningResult(HASH_TAGS.ID)
                .fetchSingle().value1();
        var tagMicronaut= dslContext.insertInto(HASH_TAGS)
                .columns(HASH_TAGS.NAME)
                .values("micronaut")
                .returningResult(HASH_TAGS.ID)
                .fetchSingle().value1();
        log.debug("Tags added: {}, {}", tagJooq, tagMicronaut);

        var id = dslContext.insertInto(POSTS)
                .columns(POSTS.TITLE, POSTS.CONTENT)
                .values("jooq test", "content of Jooq test")
                .returningResult(POSTS.ID)
                .fetchSingle().value1();
        log.debug("saved post: {}", id);

        var postTagsAdded = dslContext.insertInto(POSTS_TAGS)
                .columns(POSTS_TAGS.POST_ID, POSTS_TAGS.TAG_ID)
                .values(id, tagJooq)
                .values(id, tagMicronaut)
                .execute();
        log.debug("add post tags: {}", postTagsAdded);

        var commentsAdded = dslContext.insertInto(COMMENTS)
                .columns(COMMENTS.POST_ID, COMMENTS.CONTENT)
                .values(id, "test comments")
                .values(id, "test comments 2")
                .execute();

        log.debug("add comments: {}", commentsAdded);
        log.debug("done data initialization...");
    }
}
