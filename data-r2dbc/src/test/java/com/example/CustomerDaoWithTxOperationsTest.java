package com.example;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
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

@MicronautTest(environments = Environment.TEST, application = Application.class, startApplication = false)
@Slf4j
class CustomerDaoWithTxOperationsTest {


    @Inject
    private ApplicationContext context;

    CustomerDao customerRepository;


    @SneakyThrows
    @BeforeEach
    public void setup() {
        customerRepository = context.getBean(CustomerDaoWithTxOperations.class);
        var latch = new CountDownLatch(1);
        customerRepository.deleteAll()
                .doOnTerminate(latch::countDown)
                .subscribe(deleted -> log.debug("deleted customers: {}", deleted));
        latch.await(500, TimeUnit.MILLISECONDS);
    }

    @SneakyThrows
    @Test
    public void testInsertAndQuery() {
        var latch = new CountDownLatch(1);
        AtomicReference<UUID> id = new AtomicReference<>();
        customerRepository.save(Customer.of("customer_test", 20, Address.of("test", "NY", "210000")))
                //.doOnTerminate(latch::countDown)
                .subscribe(
                        data -> {
                            id.set(data);
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
