package com.example.persons;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

import static io.micronaut.http.HttpResponse.*;

@Controller("/persons")
@RequiredArgsConstructor
@Slf4j
public class PersonController {
    private final PersonRepository persons;

    @Get(uri = "/", produces = {MediaType.APPLICATION_JSON})
    public Flux<?> all() {
        return this.persons.findAll();
    }

    @Get(uri = "/{id}", produces = {MediaType.APPLICATION_JSON})
    public Mono<HttpResponse<?>> byId(@PathVariable ObjectId id) {
        return this.persons.findById(id).map(HttpResponse::ok);
    }

    @Post(uri = "/", consumes = {MediaType.APPLICATION_JSON})
    public Mono<HttpResponse<?>> create(@Body Person data) {
        return this.persons.insertOne(data)
                .map(id -> created(URI.create("/persons/" + id.toHexString())));
    }

    @Delete(uri = "/{id}")
    public Mono<HttpResponse<?>> delete(@PathVariable ObjectId id) {
        return this.persons.deleteById(id)
                .map(deleted -> {
                    if (deleted > 0) {
                        return noContent();
                    } else {
                        return notFound();
                    }
                });
    }
}
