package com.example

import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.annotation.Client
import io.micronaut.reactor.http.client.ReactorHttpClient
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.spock.Testcontainers
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.TimeUnit

@MicronautTest(transactional = false)
@Testcontainers
class ApplicationSpec extends Specification implements TestPropertyProvider {

    @Shared
    @AutoCleanup
    MongoDBContainer mongo = new MongoDBContainer("mongo:4")

    @Override
    Map<String, String> getProperties() {
        if (!mongo.isRunning()) {
            mongo.start()
        }
        return ["mongodb.uri": mongo.getReplicaSetUrl("mydb")]
    }

    @Inject
    EmbeddedServer application

    @Inject
    @Client("/")
    ReactorHttpClient client

    void 'test it works'() {
        expect:
        application.running
    }

    void 'get all customers'() {
        when:
        Mono<HttpResponse<List<Customer>>> responseFlux = client.exchange(HttpRequest.GET("/customers"), Argument.listOf(Customer)).log()

        then:
        StepVerifier.create(responseFlux.timeout(java.time.Duration.ofSeconds(30)))
                .consumeNextWith(s -> {
                    assert s.getStatus() == HttpStatus.OK
                    assert s.body().any { it.name == "Jack" }
                })
                .expectComplete()
                .verify()
    }

}
