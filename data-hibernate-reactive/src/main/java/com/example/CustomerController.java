package com.example;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

import static io.micronaut.http.HttpResponse.*;

@Controller("/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerRepository customerRepository;

    @Get
    HttpResponse<Flux<Customer>> all() {
        return ok(customerRepository.findAll());
    }

    @Get("{id}")
    Mono<MutableHttpResponse<Customer>> byId(@PathVariable UUID id) {
        return customerRepository.findById(id)
                .map(HttpResponse::ok)
                .switchIfEmpty(Mono.just(notFound()));
    }

    @Post
    Mono<HttpResponse<?>> save(@Body Customer customer) {
        var saved = customerRepository.save(customer);
        return saved.map(s -> created(URI.create("/customers/" + s.getId())));
    }

    @Delete("{id}")
    Mono<MutableHttpResponse<Void>> deleteById(@PathVariable UUID id) {
        return customerRepository.deleteById(id)
                .map(deleted -> {
                    if (deleted > 0) return noContent();
                    else return notFound();
                });
    }
}