package com.example.domain;

import io.micronaut.context.annotation.Factory;
import io.micronaut.data.event.listeners.PrePersistEventListener;
import io.micronaut.data.event.listeners.PreRemoveEventListener;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Factory
@Slf4j
public class PostEntityListeners {

    @Singleton
    public PrePersistEventListener<Post> prePersistEventListener() {
        return entity -> {
            log.debug("persisting post: {}", entity);
            return true;
        };
    }

    @Singleton
    public PreRemoveEventListener<Post> preRemoveEventListener() {
        return entity -> {
            log.debug("removing post: {}", entity);
            return true;
        };
    }

}
