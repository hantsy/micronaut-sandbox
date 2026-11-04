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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

// TestPropertyProvider requires PER_CLASS lifecycle (MicronautJunit5Extension.beforeAll).
@MicronautTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
class ApplicationTest implements TestPropertyProvider {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    @Inject
    EmbeddedApplication<?> application;

    @Inject
    @Client("/customers")
    ReactorHttpClient client;

    @Override
    public @NonNull Map<String, String> getProperties() {
        // MicronautJunit5Extension.beforeAll runs before the Testcontainers extension
        // auto-starts the container, so ensure it is running before reading its connection info.
        if (!postgres.isRunning()) {
            postgres.start();
        }
        return Map.of(
                "jpa.default.properties.hibernate.connection.url", postgres.getJdbcUrl(),
                "jpa.default.properties.hibernate.connection.username", postgres.getUsername(),
                "jpa.default.properties.hibernate.connection.password", postgres.getPassword()
        );
    }

    @BeforeEach
    void setup() {
        log.debug("setup......");
    }

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }

    @Test
    public void getAllCustomers() {
        log.debug("get all customers...");
        var request = HttpRequest.GET("");
        client.exchange(request, Argument.listOf(Customer.class))
                .as(StepVerifier::create)
                .consumeNextWith(res -> {
                    assertEquals(HttpStatus.OK, res.getStatus());
                    assertThat(res.body()).anyMatch(customer -> customer.getName().equals("Jack"));
                })
                .verifyComplete();
    }

}
