package com.example;

import com.example.domain.Post;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest
class IntegrationTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    EmbeddedApplication<?> application;

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }

    @Test
    void testGetAllPosts() {
        var response = client.exchange(HttpRequest.GET("/posts"), Argument.listOf(Post.class));

        var bodyFlux = Flux.from(response).map(HttpResponse::body);
        StepVerifier.create(bodyFlux)
                .consumeNextWith(posts -> assertThat(posts.size()).isEqualTo(2))
                .verifyComplete();
    }

}
