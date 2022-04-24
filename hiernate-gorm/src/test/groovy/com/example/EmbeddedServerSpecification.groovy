package com.example

import io.micronaut.context.ApplicationContext
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.reactor.http.client.ReactorHttpClient
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

abstract class EmbeddedServerSpecification extends Specification implements ConfigurationFixture, LeakageDetector {
    @AutoCleanup
    @Shared
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, configuration)

    @AutoCleanup
    @Shared
    ApplicationContext applicationContext = embeddedServer.applicationContext

    @AutoCleanup
    @Shared
    ReactorHttpClient reactorHttpClient = applicationContext.createBean(ReactorHttpClient, embeddedServer.URL)

    BlockingHttpClient getClient() {
        reactorHttpClient.toBlocking()
    }

    def cleanup() {
        assert !hasLeakage()
    }
}