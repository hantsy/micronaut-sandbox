package com.example.customers;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
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
public class CustomerController {
    private final CustomerRepository customerRepository;

    @Get(uri = "/", produces = {MediaType.APPLICATION_JSON})
    public Flux<?> all() {
        return this.customerRepository.findAll();
    }

    @Get(uri = "/{id}", produces = {MediaType.APPLICATION_JSON})
    public Mono<MutableHttpResponse<Customer>> byId(@PathVariable ObjectId id) {
        return this.customerRepository.findById(id)
                .map(HttpResponse::ok)
                .switchIfEmpty(Mono.just(notFound()));
    }

    @Post(uri = "/", consumes = {MediaType.APPLICATION_JSON})
    public Mono<HttpResponse<?>> create(@Body Customer data) {
        return this.customerRepository.insertOne(data)
                .map(id -> created(URI.create("/persons/" + id.toHexString())));
    }

    @Delete(uri = "/{id}")
    public Mono<HttpResponse<?>> delete(@PathVariable ObjectId id) {
        return this.customerRepository.deleteById(id)
                .map(deleted -> {
                    if (deleted > 0) {
                        return noContent();
                    } else {
                        return notFound();
                    }
                });
    }
}
