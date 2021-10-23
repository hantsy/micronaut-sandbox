package com.example

import com.example.persons.Person
import com.example.persons.PersonRepository
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.reactor.http.client.ReactorHttpClient
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.bson.types.ObjectId
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

@MicronautTest(environments = ["mock"])
class PersonControllerSpec extends Specification {

    @Inject
    EmbeddedApplication<?> application

    @Inject
    @Client("/")
    ReactorHttpClient client

    @Inject
    PersonRepository persons

    //for all features, run once globally
    // def setupSpec() {
    // }

    // def setup() {} //for each features

    def 'test it works'() {
        expect:
        application.running
    }

    @Unroll
    def 'max of #a and #b is #result'() {
        expect:
        Math.max(a, b) == result

        where:
        a | b  | result
        2 | 3  | 3
        3 | 10 | 10
    }

    void 'get all persons'() {
        given:
        1 * persons.findAll() >> Flux.just(Person.of(ObjectId.get(), "Jack", 40, null), Person.of(ObjectId.get(), "Rose", 20, null))

        when:
        Flux<HttpResponse<String>> resFlux = client.exchange(HttpRequest.GET("/persons"), String).log()

        then:
        //1 * persons.findAll() >> Flux.just(Person.of(ObjectId.get(), "Jack", 40, null), Person.of(ObjectId.get(), "Rose", 20, null))
        StepVerifier.create(resFlux)
        //.expectNextCount(1)
                .consumeNextWith(s -> {
                    assert s.getStatus() == HttpStatus.OK
                    assert s.body().contains('Jack')
                })
                .expectComplete()
                .verify()
    }

    void 'create a new person'() {
        given:
        def objId = ObjectId.get()
        1 * persons.insertOne(_) >> Mono.just(objId)

        when:
        def body = Person.of(null, "Jack", 40, null)
        Flux<HttpResponse<String>> resFlux = client.exchange(HttpRequest.POST("/persons", body), String).log()

        then:
        StepVerifier.create(resFlux)
                .consumeNextWith(s -> {
                    assert s.getStatus() == HttpStatus.CREATED
                    assert s.header("Location") == '/persons/' + objId.toHexString()
                })
                .expectComplete()
                .verify()
    }

    void 'get person by id '() {
        given:
        1 * persons.findById(_) >> Mono.just(Person.of(ObjectId.get(), "Jack", 40, null))

        when:
        Flux<HttpResponse<String>> resFlux = client.exchange(HttpRequest.GET("/persons/" + ObjectId.get().toHexString()), String).log()

        then:
        StepVerifier.create(resFlux)
                .consumeNextWith(s -> {
                    assert s.getStatus() == HttpStatus.OK
                    assert s.body().contains('Jack')
                })
                .expectComplete()
                .verify()
    }

    void 'get person by none-existing id '() {
        given:
        1 * persons.findById(_) >> Mono.empty()

        when:
        Flux<HttpResponse<String>> resFlux = client.exchange(HttpRequest.GET("/persons/" + ObjectId.get().toHexString()), String).log()

        then:
        StepVerifier.create(resFlux)
                .consumeErrorWith(error -> {
                    assert error instanceof HttpClientResponseException
                    assert (error as HttpClientResponseException).status == HttpStatus.NOT_FOUND
                })
                .verify()
    }

    void 'delete person by id '() {
        given:
        1 * persons.deleteById(_) >> Mono.just(1L)

        when:
        Flux<HttpResponse<String>> resFlux = client.exchange(HttpRequest.DELETE("/persons/" + ObjectId.get().toHexString()), String).log()

        then:
        StepVerifier.create(resFlux)
                .consumeNextWith(s -> {
                    assert s.getStatus() == HttpStatus.NO_CONTENT
                })
                .expectComplete()
                .verify()
    }

    void 'delete person by none-existing id '() {
        given:
        1 * persons.deleteById(_) >> Mono.just(0L)

        when:
        Flux<HttpResponse<String>> resFlux = client.exchange(HttpRequest.DELETE("/persons/" + ObjectId.get().toHexString()), String).log()

        then:
        StepVerifier.create(resFlux)
                .consumeErrorWith(error -> {
                    assert error instanceof HttpClientResponseException
                    assert (error as HttpClientResponseException).status == HttpStatus.NOT_FOUND
                })
                .verify()
    }

    @MockBean(PersonRepository)
    PersonRepository mockPersonRepository() {// must use explicit type declaration
        Mock(PersonRepository)
    }
}
