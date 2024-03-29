package com.example;

import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Singleton
@Requires(notEnv = {"mock"})
@RequiredArgsConstructor
@Slf4j
public class SampleDataInitializer {
    private final CustomerRepository customers;

    @SneakyThrows
    @EventListener()
    public void init(ServerStartupEvent event) {
        log.debug("starting data initialization...");
        var latch = new CountDownLatch(1);
        customers.deleteAll().then()
                .thenMany(
                        customers.saveAll(
                                List.of(
                                        Customer.of("Jack", 20, Address.of("test", "NY", "210000")),
                                        Customer.of("Rose", 19, Address.of("test", "Boston", "210000"))
                                )
                        )
                )
                .subscribe(
                        data -> log.debug("saved data: {}", data),
                        error -> log.error("error: {}", error),
                        () -> {
                            log.debug("done initialization");
                            latch.countDown();
                        }
                );

        latch.await(500, TimeUnit.MILLISECONDS);
    }
}
