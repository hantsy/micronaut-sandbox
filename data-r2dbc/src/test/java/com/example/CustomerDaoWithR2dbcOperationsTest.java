package com.example;

import io.micronaut.context.ApplicationContext;
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

@MicronautTest(application = Application.class, startApplication = false)
@Slf4j
class CustomerDaoWithR2dbcOperationsTest {

//    @Container
//    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:12");

    @Inject
    private ApplicationContext context;

//    @BeforeAll
//    static void beforeAll() {
//        context = ApplicationContext.run(
//                Map.of("datasources.default.url", postgreSQLContainer.getJdbcUrl(),
//                        "datasources.default.username", postgreSQLContainer.getUsername(),
//                        "datasources.default.password", postgreSQLContainer.getPassword(),
//                        "r2dbc.datasources.default.url", "r2dbc:postgresql://"
//                                + postgreSQLContainer.getHost() + ":" + postgreSQLContainer.getFirstMappedPort()
//                                + "/" + postgreSQLContainer.getDatabaseName(),
//                        "r2dbc.datasources.default.username", postgreSQLContainer.getUsername(),
//                        "r2dbc.datasources.default.password", postgreSQLContainer.getPassword()
//                )
//        );
//    }

    //@Inject
    CustomerDao customerRepository;


    @BeforeEach
    public void setup() {
        customerRepository = context.getBean(CustomerDaoWithR2dbcOperations.class);
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
