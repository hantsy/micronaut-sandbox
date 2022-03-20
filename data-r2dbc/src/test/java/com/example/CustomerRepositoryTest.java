package com.example;

import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

//@MicronautTest(application = Application.class, startApplication = false)
@Testcontainers
class CustomerRepositoryTest {

    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:12")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("init.sql"),
                    "/docker-entrypoint-initdb.d/init.sql"
            );

    private static ApplicationContext context;

    @BeforeAll
    static void beforeAll() {
        context = ApplicationContext.run(
                Map.of("datasources.default.url", postgreSQLContainer.getJdbcUrl(),
                        "datasources.default.username", postgreSQLContainer.getUsername(),
                        "datasources.default.password", postgreSQLContainer.getPassword(),
                        "r2dbc.datasources.default.url", "r2dbc:postgresql://"
                                + postgreSQLContainer.getHost() + ":" + postgreSQLContainer.getFirstMappedPort()
                                + "/" + postgreSQLContainer.getDatabaseName(),
                        "r2dbc.datasources.default.username", postgreSQLContainer.getUsername(),
                        "r2dbc.datasources.default.password", postgreSQLContainer.getPassword()
                )
        );

        // run Flyway.migrate to create schema.
        //context.getBean(Flyway.class).migrate();

    }

    //@Inject
    CustomerRepository customerRepository;


    @BeforeEach
    public void setup() {
        customerRepository = context.getBean(CustomerRepository.class);
    }

    @Test
    public void testInsertAndQuery() {
        customerRepository.save(Customer.of("customer_test", 20, Address.of("test", "NY", "210000")))
                .flatMap(id -> customerRepository.findById(id))
                .as(StepVerifier::create)
                .consumeNextWith(it -> assertThat(it.name()).isEqualTo("customer_test"))
                .verifyComplete();
    }
}
