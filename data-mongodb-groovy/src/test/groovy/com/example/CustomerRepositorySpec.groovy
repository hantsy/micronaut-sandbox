package com.example

import groovy.util.logging.Slf4j
import io.micronaut.configuration.mongo.core.MongoSettings
import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

@MicronautTest(startApplication = false, transactional = false)
@Testcontainers
@Slf4j
class CustomerRepositorySpec extends Specification {
    @Shared
    @AutoCleanup
    ApplicationContext applicationContext

    //starting a postgres in docker with testcontainers.
    @Shared
    @AutoCleanup
    MongoDBContainer mongo = new MongoDBContainer("mongo:4")

    def setupSpec() {
        applicationContext = ApplicationContext.builder((MongoSettings.MONGODB_URI): "mongodb://${mongo.host}:${mongo.firstMappedPort}/mydb")
        // .mainClass(CustomerRepositorySpec)
                .build()
                .start()
    }

    //@Inject
    CustomerRepository customerRepository

    def setup() {
        customerRepository = applicationContext.getBean(CustomerRepository)
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
