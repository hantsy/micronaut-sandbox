package com.example;

import io.micronaut.context.env.Environment;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest(application = Application.class, environments = Environment.TEST, startApplication = false)
@Slf4j
class CustomerRepositoryTest {

    @Inject
    CustomerRepository customerRepository;

    @BeforeEach
    public void setup() {
        log.debug("setup...");
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
                            id.set(data.id());
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
                .consumeNextWith(it -> assertThat(it.name()).isEqualTo("customer_test"))
                .verifyComplete();

        customerRepository.findAll()
                .as(StepVerifier::create)
                .consumeNextWith(it -> assertThat(it.name()).isEqualTo("customer_test"))
                .verifyComplete();
    }


}
