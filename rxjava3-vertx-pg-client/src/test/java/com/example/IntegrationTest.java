package com.example;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.rxjava3.http.client.Rx3HttpClient;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest
class IntegrationTest {

    @Inject
    EmbeddedApplication<?> application;

    @Inject
    @Client("/posts")
    Rx3HttpClient httpClient;

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
        latch.await(1000, TimeUnit.MILLISECONDS);

        assertThat(replay.get().size()).isGreaterThan(0);
    }

}
