package com.example;

import com.example.persons.PersonRepository;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Requires(notEnv = "mock")
@Slf4j
@RequiredArgsConstructor
public class DataInitializer {
    private final PersonRepository persons;

    @EventListener
    public void onStart(StartupEvent event) {
        log.debug("starting data initialization...");
        this.persons.init();
    }
}
