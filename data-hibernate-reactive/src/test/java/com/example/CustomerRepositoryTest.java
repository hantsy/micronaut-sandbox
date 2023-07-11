package com.example;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.support.TestPropertyProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
class CustomerRepositoryTest  {

    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:14");

    static ApplicationContext context;

    @BeforeAll
    public static void setupAll() {
        postgreSQLContainer.start();
        context = ApplicationContext
                .run(
                        Map.of(
                                "jpa.default.reactive", "true",
                                "jpa.default.properties.hibernate.connection.url", postgreSQLContainer.getJdbcUrl(),
                                "jpa.default.properties.hibernate.connection.username", postgreSQLContainer.getUsername(),
                                "jpa.default.properties.hibernate.connection.password", postgreSQLContainer.getPassword()
                        )
                )
                .start();

    }

    @AfterAll
    public static void afterAll() {
        if(context.isRunning()) {
            context.stop();
        }
        if(postgreSQLContainer.isRunning()) {
            postgreSQLContainer.stop();
        }
    }

    CustomerRepository customerRepository;

    @BeforeEach
    public void setup() {
        log.debug("setup...");
        this.customerRepository = context.getBean(CustomerRepository.class);
    }

    @lombok.SneakyThrows
    @Test
    public void testInsertAndQuery() {
        var latch = new CountDownLatch(1);
        AtomicReference<UUID> id = new AtomicReference<>();
        customerRepository.save(Customer.of("customer_test", 20, Address.of("test", "NY", "210000")))
                //.doOnTerminate(latch::countDown)
                .subscribe(
                        data -> {
                            id.set(data.getId());
                            latch.countDown();
                        },
                        err -> log.error("error", err),
                        () -> log.debug("done for inserting")
                );
        latch.await(1000, TimeUnit.MILLISECONDS);

        var uuid = id.get();
        assertNotNull(uuid);
        log.debug("generated id: {}", uuid);

        customerRepository.findById(uuid)
                .as(StepVerifier::create)
                .consumeNextWith(it -> assertThat(it.getName()).isEqualTo("customer_test"))
                .verifyComplete();

        customerRepository.findAll()
                .as(StepVerifier::create)
                .consumeNextWith(it -> assertThat(it.getName()).isEqualTo("customer_test"))
                .verifyComplete();
    }
}