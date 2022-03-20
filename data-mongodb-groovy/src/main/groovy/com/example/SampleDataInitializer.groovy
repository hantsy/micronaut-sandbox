package com.example

import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class SampleDataInitializer {
    def log = LoggerFactory.getLogger(SampleDataInitializer)

    @Inject
    CustomerRepository customerRepository

    @EventListener
    def init(ServerStartupEvent event) {
        customerRepository.deleteAll()
        customerRepository.saveAll([
                Customer.of("Jack", 20, Address.of("test", "test", "test")),
                Customer.of("Rose", 18, Address.of("test", "test", "test"))
        ])
        customerRepository.findAll { it -> log.debug("customer : $it") }
    }
}
