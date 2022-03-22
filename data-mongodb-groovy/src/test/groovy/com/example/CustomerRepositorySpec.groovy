package com.example

import groovy.util.logging.Slf4j
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.testcontainers.containers.GenericContainer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

@MicronautTest(startApplication = false, transactional = false)
@Slf4j
class CustomerRepositorySpec extends Specification {

    //starting a postgres in docker with testcontainers.
    @Shared
    @AutoCleanup
    GenericContainer mongo = new GenericContainer("mongo")
            .withExposedPorts(27017)

    def setupSpec() {
        mongo.start()
    }

    @Inject
    EmbeddedApplication<?> application

    @Inject
    CustomerRepository customerRepository;

    def setup() {
        customerRepository.deleteAll()
    }

    void 'application is not running'() {
        expect:
        !application.running
    }

    void 'test findAll'() {
        given:
        this.customerRepository.saveAll(List.of(Customer.of("Jack", 40, null)))
                .forEach(it -> log.debug(" saved customer: {}", it))

        when:
        def result = this.customerRepository.findAll()

        then:
        result
        result.size() == 1
    }
}
