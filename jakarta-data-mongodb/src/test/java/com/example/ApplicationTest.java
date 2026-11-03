package com.example;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.reactor.http.client.ReactorHttpClient;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// TestPropertyProvider requires PER_CLASS lifecycle (MicronautJunit5Extension.beforeAll).
@MicronautTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApplicationTest implements TestPropertyProvider {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:4"));

    @Inject
    EmbeddedApplication<?> application;

    @Inject
    @Client("/customers")
    ReactorHttpClient client;

    @Override
    public @NonNull Map<String, String> getProperties() {
        // MicronautJunit5Extension.beforeAll runs before the Testcontainers extension
        // auto-starts the container, so ensure it is running before reading its host/port.
        if (!mongo.isRunning()) {
            mongo.start();
        }
        return Map.of("mongodb.uri", mongo.getReplicaSetUrl() + "/mydb");
    }

    @Test
    void testItWorks() {
        assertTrue(application.isRunning());
    }

    @Test
    void getAllCustomers() {
        var request = HttpRequest.GET("");
        client.exchange(request, Argument.listOf(Customer.class))
                .as(StepVerifier::create)
                .consumeNextWith(res -> {
                    assertEquals(HttpStatus.OK, res.getStatus());
                    assertThat(res.body()).anyMatch(customer -> customer.name().equals("Jack"));
                })
                .verifyComplete();
    }

}
