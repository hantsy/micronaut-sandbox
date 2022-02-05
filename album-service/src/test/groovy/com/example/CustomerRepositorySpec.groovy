package com.example


import com.example.customers.Customer
import com.example.customers.CustomerRepository
import groovy.util.logging.Slf4j
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import reactor.test.StepVerifier
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@MicronautTest(startApplication = false)
@Slf4j
class CustomerRepositorySpec extends Specification {
//
// starting a postgres in docker with testcontainers.
//
//    @Shared
//    @AutoCleanup
//    GenericContainer mongo = new GenericContainer("mongo")
//            .withExposedPorts(27017)
//
//    def setupSpec() {
//        mongo.start()
//    }


    @Inject
    EmbeddedApplication<?> application

    @Inject
    CustomerRepository customerRepository;

    def setup() {
        CountDownLatch latch = new CountDownLatch(1)
        customerRepository.deleteAll()
                .doOnTerminate(_ -> latch.countDown())
                .subscribe(it -> log.debug "deleted customers: {}", it)
        latch.await(1000, TimeUnit.MILLISECONDS)
    }

    void 'application is not running'() {
        expect:
        !application.running
    }

    void 'test findAll'() {
        given:
        this.customerRepository.insertMany(List.of(Customer.of("Jack", 40, null)))
                .block(Duration.ofMillis(5000L))

        when:
        def result = this.customerRepository.findAll()

        then:
        StepVerifier.create(result)
                .expectNextMatches(it -> it.name == "Jack")
                .expectComplete()
                .verify()
    }
}
