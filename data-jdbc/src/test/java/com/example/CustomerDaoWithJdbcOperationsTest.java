package com.example;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(application = Application.class, startApplication = false)
class CustomerDaoWithJdbcOperationsTest {

    @Inject
    private ApplicationContext context;

    CustomerDao customerRepository;

    @BeforeEach
    public void setup() {
        customerRepository = context.getBean(CustomerDaoWithJdbcOperations.class);
    }

    @Test
    public void testInsertAndQuery() {
        var savedId = customerRepository.save(Customer.of("customer_test", 20, Address.of("test", "NY", "210000")));
        assertNotNull(savedId);
        var found = customerRepository.findById(savedId);
        assertTrue(found.isPresent());
        found.ifPresent(it -> assertThat(it.name()).isEqualTo("customer_test"));
    }
}
