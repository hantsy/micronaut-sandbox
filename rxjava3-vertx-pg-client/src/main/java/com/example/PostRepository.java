package com.example;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@RequiredArgsConstructor
@Singleton
public class PostRepository {
    private static Function<Row, Post> MAPPER = (row) ->
            new Post(
                    row.getUUID("id"),
                    row.getString("title"),
                    row.getString("content"),
                    row.getLocalDateTime("created_at")
            );


    private final Pool client;

    public Flowable<Post> findAll() {
        return this.client
                .query("SELECT * FROM posts")
                .rxExecute()
                .flattenAsFlowable(
                        rows -> StreamSupport.stream(rows.spliterator(), false)
                                .map(MAPPER)
                                .collect(Collectors.toList())
                );
    }


    public Single<Post> findById(UUID id) {
        Objects.requireNonNull(id, "id can not be null");
        return client.preparedQuery("SELECT * FROM posts WHERE id=$1").rxExecute(Tuple.of(id))
                .map(RowSet::iterator)
                .flatMap(iterator -> iterator.hasNext() ? Single.just(MAPPER.apply(iterator.next()))
                        : Single.error(new PostNotFoundException(id))
                );
    }

    public Single<UUID> save(Post data) {
        return client.preparedQuery("INSERT INTO posts(title, content) VALUES ($1, $2) RETURNING (id)")
                .rxExecute(Tuple.of(data.title(), data.content()))
                .map(rs -> rs.iterator().next().getUUID("id"));
    }

    public Single<Integer> saveAll(List<Post> data) {
        var tuples = data.stream()
                .map(d -> Tuple.of(d.title(), d.content()))
                .toList();

        return client.preparedQuery("INSERT INTO posts (title, content) VALUES ($1, $2)")
                .rxExecuteBatch(tuples)
                .map(SqlResult::rowCount);
    }

    public Single<Integer> update(Post data) {
        return client.preparedQuery("UPDATE posts SET title=$1, content=$2 WHERE id=$3")
                .rxExecute(Tuple.of(data.title(), data.content(), data.id()))
                .map(SqlResult::rowCount);
    }

    public Single<Integer> deleteAll() {
        return client.query("DELETE FROM posts").rxExecute()
                .map(SqlResult::rowCount);
    }

    public Single<Integer> deleteById(UUID id) {
        Objects.requireNonNull(id, "id can not be null");
        return client.preparedQuery("DELETE FROM posts WHERE id=$1").rxExecute(Tuple.of(id))
                .map(SqlResult::rowCount);
    }
}
