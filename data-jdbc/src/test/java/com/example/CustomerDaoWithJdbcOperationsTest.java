package com.example;

import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

//@MicronautTest(application = Application.class, startApplication = false)
@Testcontainers
class CustomerDaoWithJdbcOperationsTest {

    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:12");
//            .withCopyFileToContainer(
//                    MountableFile.forClasspathResource("init.sql"),
//                    "/docker-entrypoint-initdb.d/init.sql"
//            );

    private static ApplicationContext context;

    @BeforeAll
    static void beforeAll() {
        context = ApplicationContext.run(
                Map.of("datasources.default.url", postgreSQLContainer.getJdbcUrl(),
                        "datasources.default.username", postgreSQLContainer.getUsername(),
                        "datasources.default.password", postgreSQLContainer.getPassword(),
                        "datasources.default.driverClassName","org.postgresql.Driver"
                )
        );

    }

    //@Inject
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
