package com.example;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@MicronautTest
@Slf4j
class IntegrationTestsWithJava11HttpClient {
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final HttpClient httpClient = HttpClient.newBuilder()
            .executor(executorService)
            .version(HttpClient.Version.HTTP_2)
            .build();

    @Inject
    EmbeddedServer embeddedServer;

    @Test
    void serverIsRunning() {
        assertThat(embeddedServer.isRunning()).isTrue();
    }

    @Test
    void testGetAllPosts() {
        this.httpClient
                .sendAsync(
                        HttpRequest.newBuilder()
                                .GET()
                                .uri(URI.create(embeddedServer.getURL() + "/posts"))
                                .header("Accept", "application/json")
                                .build()
                        ,
                        HttpResponse.BodyHandlers.ofString()
                )
                .thenAccept(response -> {
                    log.debug("response body: {}", response.body());
                    assertThat(response.statusCode()).isEqualTo(200);
                    assertThat(response.body()).containsSequence("Micronaut");
                })
                .exceptionally(e -> {
                    log.debug("exception: {}", e);
                    return null;
                })
                .join();
    }


}
