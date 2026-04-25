package com.example;

import io.micronaut.context.env.Environment;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@MicronautTest(application = Application.class, environments = Environment.TEST, startApplication = false)
@Slf4j
class OrderRepositoryTest {

    @Inject
    OrderRepository orders;

    @Test
    void findAllOrdersByCustomer() {
        //TODO
    }

    @Test
    void findByOrderId() {
        // TODO
    }
}