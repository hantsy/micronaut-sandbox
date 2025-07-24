package com.example;

import io.micronaut.context.env.Environment;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(application = Application.class, environments = Environment.TEST, startApplication = false)
@Slf4j
class CustomerRepositoryTest {

    @Inject
    CustomerRepository customerRepository;

    @BeforeEach
    public void setup() {
        log.debug("setup....");
    }

    @Test
    public void testInsertAndQuery() {
        var saved= customerRepository.save(Customer.of("customer_test", 20, Address.of("test", "NY", "210000")));
        assertNotNull(saved);
        var found = customerRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        found.ifPresent(it -> assertThat(it.getName()).isEqualTo("customer_test"));
    }
}
