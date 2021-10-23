package com.example

import com.example.persons.PersonRepository
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import reactor.test.StepVerifier
import spock.lang.Specification

@MicronautTest(startApplication = false)
class PersonRepositorySpec extends Specification {
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
    PersonRepository persons;

    void 'application is not running'() {
        expect:
        !application.running
    }

    void 'test findAll'() {
        when:
        def result = this.persons.findAll()

        then:
        StepVerifier.create(result)
                .expectNextCount(3)
                .expectComplete()
                .verify()
    }
}
