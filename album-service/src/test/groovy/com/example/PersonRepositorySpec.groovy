package com.example

import com.example.persons.PersonRepository
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import reactor.test.StepVerifier
import spock.lang.Specification

@MicronautTest
class PersonRepositorySpec extends Specification {

//    @Shared
//    @AutoCleanup
//    GenericContainer mongo = new GenericContainer("mongo")
//            .withExposedPorts(27017)
//
//    def setupSpec() {
//        mongo.start()
//    }

    @Inject
    PersonRepository persons;

    void 'test it works'() {
        when:
        def result = this.persons.findAll()

        then:
        StepVerifier.create(result)
                .expectNextCount(3)
                .expectComplete()
    }
}
