package com.example

import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.annotation.Client
import io.micronaut.reactor.http.client.ReactorHttpClient
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import spock.lang.Specification

@MicronautTest(transactional = false)
class ApplicationSpec extends Specification {

    @Inject
    EmbeddedApplication<?> application

    @Inject
    @Client("/")
    ReactorHttpClient client

    void 'test it works'() {
        expect:
        application.running
    }

    void 'get all customers'() {
        when:
        Flux<HttpResponse<List<Customer>>> responseFlux = client.exchange(HttpRequest.GET("/customers"), Argument.listOf(Customer)).log()

        then:
        StepVerifier.create(responseFlux)
                .consumeNextWith(s -> {
                    assert s.getStatus() == HttpStatus.OK
                    assert s.body().any { it.name == "Jack" }
                })
                .expectComplete()
                .verify()
    }

}
