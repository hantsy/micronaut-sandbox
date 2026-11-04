package com.example;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class SampleDataInitializer {

    private final CustomerRepository customerRepository;

    @EventListener
    public void init(ServerStartupEvent event) {
        customerRepository.deleteAll();
        customerRepository.saveAll(List.of(
                Customer.of("Jack", 20, Address.of("test", "test", "test")),
                Customer.of("Rose", 18, Address.of("test", "test", "test"))
        ));
        customerRepository.findAll().forEach(c -> log.debug("customer: {}", c));
    }
}
