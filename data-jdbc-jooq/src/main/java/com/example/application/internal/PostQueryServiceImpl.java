package com.example.application.internal;

import com.example.application.PaginatedResult;
import com.example.application.PostDetails;
import com.example.application.PostQueryService;
import com.example.application.PostSummary;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.util.UUID;

import static com.example.demo.jooq.Tables.COMMENTS;
import static com.example.demo.jooq.Tables.HASH_TAGS;
import static com.example.demo.jooq.Tables.POSTS;
import static com.example.demo.jooq.Tables.POSTS_TAGS;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

@Singleton
@Transactional
@Slf4j
@RequiredArgsConstructor
public class PostQueryServiceImpl implements PostQueryService {
    private DSLContext dslContext;

    @Override
    public PaginatedResult<PostSummary> findPostsByKeyword(String keyword, int offset, int limit) {
        log.debug("searching for posts with keyword: {}, offset: {}, limit: {}", keyword, offset, limit);

        var p = POSTS;
        var pt = POSTS_TAGS;
        var t = HASH_TAGS;
        var c = COMMENTS;

        Condition where = DSL.trueCondition();
        if (StringUtils.hasText(keyword)) {
            where = where.and(p.TITLE.likeIgnoreCase("%" + keyword + "%"));
        }
        var data = dslContext.select(
                        p.ID,
                        p.TITLE,
                        DSL.field("count(comments.id)", SQLDataType.BIGINT),
                        multiset(select(t.NAME)
                                .from(t)
                                .join(pt).on(t.ID.eq(pt.TAG_ID))
                                .where(pt.POST_ID.eq(p.ID))
                        ).as("tags")
                )
                .from(p.leftJoin(c).on(c.POST_ID.eq(p.ID)))
                .where(where)
                .groupBy(p.ID)
                .orderBy(p.CREATED_AT)
                .limit(offset, limit)
                .fetchInto(PostSummary.class);

        var count = dslContext.select(DSL.field("count(*)", SQLDataType.BIGINT))
                .from(p)
                .where(where)
                .fetchSingle().value1();

        log.debug("found posts: {}, count: {}", data, count);
        return new PaginatedResult<>(data, count);
    }

    @Override
    public PostDetails getPostDetailsById(UUID postId) {
        log.debug("searching for post with id: {}", postId);
        var p = POSTS;
        var pt = POSTS_TAGS;
        var t = HASH_TAGS;
        var c = COMMENTS;
        return dslContext
                .select(
                        p.ID,
                        p.TITLE,
                        p.CONTENT,
                        p.CREATED_AT,
                        p.UPDATED_AT,
                        DSL.field("count(comments.id)", SQLDataType.BIGINT),
                        DSL.row(select(c.CONTENT, c.CREATED_AT)
                                .from(c)
                                .where(c.POST_ID.eq(p.ID))
                                .orderBy(c.CREATED_AT.desc())
                                .limit(0, 1)
                        )
                )
                .from(p.leftJoin(c).on(c.POST_ID.eq(p.ID)))
                .where(p.ID.eq(postId))
                .orderBy(p.CREATED_AT.desc())
                .fetchOneInto(PostDetails.class);
    }
}
