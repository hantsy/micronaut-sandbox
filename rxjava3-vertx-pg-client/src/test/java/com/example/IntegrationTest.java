package com.example;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.rxjava3.http.client.Rx3HttpClient;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

// TestPropertyProvider requires PER_CLASS lifecycle (MicronautJunit5Extension.beforeAll).
@MicronautTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IntegrationTest implements TestPropertyProvider {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("blogdb")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("init.sql"),
                    "/docker-entrypoint-initdb.d/init.sql"
            );

    @Inject
    EmbeddedApplication<?> application;

    @Inject
    @Client("/posts")
    Rx3HttpClient httpClient;

    @Override
    public @NonNull Map<String, String> getProperties() {
        if (!postgres.isRunning()) {
            postgres.start();
        }
        Map<String, String> props = new HashMap<>();
        props.put("datasources.default.url", postgres.getJdbcUrl());
        props.put("datasources.default.username", postgres.getUsername());
        props.put("datasources.default.password", postgres.getPassword());
        props.put("vertx.pg.client.host", postgres.getHost());
        props.put("vertx.pg.client.port", String.valueOf(postgres.getFirstMappedPort()));
        props.put("vertx.pg.client.user", postgres.getUsername());
        props.put("vertx.pg.client.password", postgres.getPassword());
        props.put("vertx.pg.client.database", "blogdb");
        return props;
    }

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }

    @SneakyThrows
    @Test
    void testGetAllPosts() {
        var request = HttpRequest.GET("");
        var replay = new AtomicReference<List<Post>>();
        var latch = new CountDownLatch(1);
        httpClient.exchange(request, Argument.listOf(Post.class))
                .doOnComplete(latch::countDown)
                .doOnError(System.err::println)
                .subscribe(data -> replay.set(data.body()));
        latch.await(10, TimeUnit.SECONDS);

        assertThat(replay.get().size()).isGreaterThan(0);
    }

}
