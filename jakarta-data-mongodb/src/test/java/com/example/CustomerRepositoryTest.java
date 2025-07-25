package com.example;

import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(application = Application.class, environments = Environment.TEST, startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
@Slf4j
class CustomerRepositoryTest implements TestPropertyProvider {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @Override
    public @NonNull Map<String, String> getProperties() {
        if (!mongoDBContainer.isRunning()) {
            mongoDBContainer.start();
        }
        return Map.of("mongodb.uri", mongoDBContainer.getReplicaSetUrl());
    }

    @Inject
    CustomerRepository customerRepository;

    @BeforeEach
    public void setup() {
        log.debug("setup....");
    }

    @Test
    public void testInsertAndQuery() {
        var saved = customerRepository.save(Customer.of("customer_test", 20, Address.of("test", "NY", "210000")));
        assertNotNull(saved);
        var found = customerRepository.findById(saved.id());
        assertTrue(found.isPresent());
        found.ifPresent(it -> assertThat(it.name()).isEqualTo("customer_test"));
    }
}
