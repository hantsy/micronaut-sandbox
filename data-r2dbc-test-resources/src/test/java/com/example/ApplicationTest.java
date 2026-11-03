package com.example;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.reactor.http.client.ReactorHttpClient;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

//  transactional = true will throw:
//  Transaction mode is not supported when the synchronous transaction manager is created using Reactive transaction manager!
@MicronautTest(application = Application.class, transactional = false)
@Slf4j
class ApplicationTest {

    @Inject
    EmbeddedApplication<?> application;

    @Inject
    @Client("/customers")
    ReactorHttpClient client;

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }

    @Test
    public void getAllCustomers() {
        log.debug("get all customers...");
        var request = HttpRequest.GET("");
        client.exchange(request, Argument.listOf(Customer.class))
                .log()
                .as(StepVerifier::create)
                .consumeNextWith(res -> {
                    assertEquals(HttpStatus.OK, res.getStatus());
                    assertThat(res.body()).anyMatch(customer -> customer.name().equals("Jack"));
                })
                .verifyComplete();
    }

}
