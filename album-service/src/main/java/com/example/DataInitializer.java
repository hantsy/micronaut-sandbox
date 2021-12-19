package com.example;

import com.example.customers.CustomerRepository;
import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Requires(notEnv = "mock")
@Slf4j
@RequiredArgsConstructor
public class DataInitializer {
    private final CustomerRepository customerRepository;

    @EventListener
    public void onStart(ServerStartupEvent event) {
        log.debug("starting data initialization...");
        this.customerRepository.init();
    }
}
