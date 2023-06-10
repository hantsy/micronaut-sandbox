package com.example

import groovy.util.logging.Slf4j
import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

@MicronautTest(startApplication = false, transactional = false)
@Slf4j
class CustomerRepositorySpec extends Specification {
//    @Shared
//    @AutoCleanup
//    ApplicationContext applicationContext
//
//    //starting a postgres in docker with testcontainers.
//    @Shared
//    @AutoCleanup
//    GenericContainer mongo =
//            new GenericContainer("mongo:4.0")
//                    .withExposedPorts(27017)
//
//
//
//    def setupSpec() {
//        mongo.start()
//        applicationContext = ApplicationContext.builder((MongoSettings.MONGODB_URI): "mongodb://${mongo.containerIpAddress}:${mongo.getMappedPort(27017)}/mydb")
//                .mainClass(CustomerRepositorySpec)
//                .start()
//    }
//

    @Inject
    CustomerRepository customerRepository;

    def setup() {
        // customerRepository = applicationContext.getBean(CustomerRepository)
        customerRepository.deleteAll()
    }

    void 'application is not running'() {
        expect:
        !applicationContext.getBean(EmbeddedApplication).running
    }

    void 'test findAll'() {
        given:
        customerRepository
                .saveAll(
                        [
                                Customer.of("Jack", 40, null),
                                Customer.of("Rose", 39, null)
                        ]
                )
                .forEach(it -> log.debug(" saved customer: {}", it))

        when:
        def result = this.customerRepository.findAll()

        then:
        result
        result.size() == 2
        result.any { it.name == "Jack" }
    }
}
