package com.example

import io.micronaut.core.type.Argument
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
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

@MicronautTest(environments = ["mock"])
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
        1 * customerRepository.findAll() >> List.of(Customer.of("Jack", 40, null), Customer.of("Rose", 20, null))

        when:
        Mono<HttpResponse<List<Customer>>> responseFlux = client.exchange(HttpRequest.GET("/customers"), Argument.listOf(Customer)).log()

        then:
        StepVerifier.create(responseFlux)
                .consumeNextWith(s -> {
                    assert s.getStatus() == HttpStatus.OK
                    assert s.body().any { it.name == "Jack" }
                })
                .expectComplete()
                .verify()
    }

    void 'create a new customer'() {
        given:
        def objId = ObjectId.get()
        1 * customerRepository.save(_) >> new Customer(id: objId.toString(), name: "Jack", age: 20, address: null)

        when:
        def body = Customer.of("Jack", 40, null)
        Mono<HttpResponse<Void>> resFlux = client.exchange(HttpRequest.POST("/customers", body), Void).log()

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
        1 * customerRepository.findById(_) >> Optional.of(Customer.of("Jack", 40, null))

        when:
        Mono<HttpResponse<Customer>> resFlux = client.exchange(HttpRequest.GET("/customers/" + ObjectId.get().toHexString()), Argument.of(Customer)).log()

        then:
        StepVerifier.create(resFlux)
                .consumeNextWith(s -> {
                    assert s.getStatus() == HttpStatus.OK
                    assert s.body().name == 'Jack'
                })
                .expectComplete()
                .verify()
    }

    void 'get customer by none-existing id '() {
        given:
        1 * customerRepository.findById(_) >> Optional.ofNullable(null)

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
        1 * customerRepository.deleteById(_)

        when:
        Mono<HttpResponse<Void>> resFlux = client.exchange(HttpRequest.DELETE("/customers/" + ObjectId.get().toHexString()), Void).log()

        then:
        StepVerifier.create(resFlux)
                .consumeNextWith(s -> {
                    assert s.getStatus() == HttpStatus.NO_CONTENT
                })
                .expectComplete()
                .verify()
    }


    @MockBean(CustomerRepository)
    CustomerRepository mockedCustomerRepository() {// must use explicit type declaration
        Mock(CustomerRepository)
    }
}
