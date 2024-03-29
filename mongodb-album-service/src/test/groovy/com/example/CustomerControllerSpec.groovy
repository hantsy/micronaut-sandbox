package com.example

import com.example.customers.Customer
import com.example.customers.CustomerRepository
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

@MicronautTest(environments = ["mock"], transactional = false)
class CustomerControllerSpec extends Specification {

    @Inject
    EmbeddedApplication<?> application

    @Inject
    @Client("/")
    ReactorHttpClient client

    @Inject
    CustomerRepository customerRepository

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

    void 'get all customers'() {
        given:
        1 * customerRepository.findAll() >> Flux.just(Customer.of(ObjectId.get(), "Jack", 40, null), Customer.of(ObjectId.get(), "Rose", 20, null))

        when:
        Mono<HttpResponse<String>> resFlux = client.exchange(HttpRequest.GET("/customers"), String).log()

        then:
        //1 * customers.findAll() >> Flux.just(Customer.of(ObjectId.get(), "Jack", 40, null), Customer.of(ObjectId.get(), "Rose", 20, null))
        StepVerifier.create(resFlux)
        //.expectNextCount(1)
                .consumeNextWith(s -> {
                    assert s.getStatus() == HttpStatus.OK
                    assert s.body().contains('Jack')
                })
                .expectComplete()
                .verify()
    }

    void 'create a new customer'() {
        given:
        def objId = ObjectId.get()
        1 * customerRepository.insertOne(_) >> Mono.just(objId)

        when:
        def body = Customer.of(null, "Jack", 40, null)
        Mono<HttpResponse<String>> resFlux = client.exchange(HttpRequest.POST("/customers", body), String).log()

        then:
        StepVerifier.create(resFlux)
                .consumeNextWith(s -> {
                    assert s.getStatus() == HttpStatus.CREATED
                    assert s.header("Location") == '/customers/' + objId.toHexString()
                })
                .expectComplete()
                .verify()
    }

    void 'get customer by id '() {
        given:
        1 * customerRepository.findById(_) >> Mono.just(Customer.of(ObjectId.get(), "Jack", 40, null))

        when:
        Mono<HttpResponse<String>> resFlux = client.exchange(HttpRequest.GET("/customers/" + ObjectId.get().toHexString()), String).log()

        then:
        StepVerifier.create(resFlux)
                .consumeNextWith(s -> {
                    assert s.getStatus() == HttpStatus.OK
                    assert s.body().contains('Jack')
                })
                .expectComplete()
                .verify()
    }

    void 'get customer by none-existing id '() {
        given:
        1 * customerRepository.findById(_) >> Mono.empty()

        when:
        Mono<HttpResponse<String>> resFlux = client.exchange(HttpRequest.GET("/customers/" + ObjectId.get().toHexString()), String).log()

        then:
        StepVerifier.create(resFlux)
                .consumeErrorWith(error -> {
                    assert error instanceof HttpClientResponseException
                    assert (error as HttpClientResponseException).status == HttpStatus.NOT_FOUND
                })
                .verify()
    }

    void 'delete customer by id '() {
        given:
        1 * customerRepository.deleteById(_) >> Mono.just(1L)

        when:
        Mono<HttpResponse<String>> resFlux = client.exchange(HttpRequest.DELETE("/customers/" + ObjectId.get().toHexString()), String).log()

        then:
        StepVerifier.create(resFlux)
                .consumeNextWith(s -> {
                    assert s.getStatus() == HttpStatus.NO_CONTENT
                })
                .expectComplete()
                .verify()
    }

    void 'delete customer by none-existing id '() {
        given:
        1 * customerRepository.deleteById(_) >> Mono.just(0L)

        when:
        Mono<HttpResponse<String>> resFlux = client.exchange(HttpRequest.DELETE("/customers/" + ObjectId.get().toHexString()), String).log()

        then:
        StepVerifier.create(resFlux)
                .consumeErrorWith(error -> {
                    assert error instanceof HttpClientResponseException
                    assert (error as HttpClientResponseException).status == HttpStatus.NOT_FOUND
                })
                .verify()
    }

    @MockBean(CustomerRepository)
    CustomerRepository mockedCustomerRepository() {// must use explicit type declaration
        Mock(CustomerRepository)
    }
}
