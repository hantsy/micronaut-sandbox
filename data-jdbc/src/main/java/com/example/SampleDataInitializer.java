package com.example;

import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Singleton
@Requires(notEnv = "mock")
@RequiredArgsConstructor
@Slf4j
public class SampleDataInitializer {
    private final CustomerRepository customers;

    @EventListener()
    public void init(ServerStartupEvent event) {
        log.debug("start data initialization...");
        customers.deleteAll();
        customers.saveAll(
                List.of(
                        Customer.of("Jack", 20, Address.of("test", "NY", "210000")),
                        Customer.of("Rose", 19, Address.of("test", "Boston", "210000"))
                )
        );
        customers.findAll().forEach(c -> log.debug("saved customer: {}", c));
        log.debug("done data initialization...");
    }
}
