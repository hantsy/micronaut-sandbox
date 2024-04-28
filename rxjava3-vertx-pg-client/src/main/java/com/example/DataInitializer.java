package com.example;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Singleton
//@Requires(beans = Flyway.class)
public class DataInitializer {

    private final PostRepository postRepository;

    @EventListener()
    public void init(ServerStartupEvent event) {
        postRepository.deleteAll()
                .flatMap(deleted -> {
                    log.debug("deleted posts: {}", deleted);
                    return postRepository.saveAll(
                            List.of(
                                    new Post(null, "Spring and Micronaut", "content", null),
                                    new Post(null, "Jakarta EE and Micronaut", "content", null)
                            )
                    );
                })
                .flatMapPublisher(saved -> {
                    log.debug("saved posts: {}", saved);
                    return postRepository.findAll();
                })
                .subscribe(System.out::println);
    }
}
