package com.example;

import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

//@MicronautTest(application = Application.class, startApplication = false)
@Testcontainers
class CustomerDaoTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    private static ApplicationContext context;

    @BeforeAll
    static void beforeAll() {
        context = ApplicationContext.run(
                Map.of("mongodb.uri", mongoDBContainer.getReplicaSetUrl())
        );
    }

    CustomerDao customerRepository;

    @BeforeEach
    public void setup() {
        customerRepository = context.getBean(CustomerDao.class);
    }

    @Test
    public void testInsertAndQuery() {
        var savedCustomer = customerRepository.save(Customer.of("customer_test", 20, Address.of("test", "NY", "210000")));
        assertNotNull(savedCustomer);
        var found = customerRepository.findById(savedCustomer.id());
        assertTrue(found.isPresent());
        found.ifPresent(it -> assertThat(it.name()).isEqualTo("customer_test"));
    }
}
