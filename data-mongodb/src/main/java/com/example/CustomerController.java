package com.example;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import lombok.RequiredArgsConstructor;

import java.net.URI;

@Controller("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;

    @Get
    public HttpResponse<Iterable<Customer>> all() {
        return HttpResponse.ok(customerRepository.findAll());
    }

    @Get("/{id}")
    public HttpResponse<Customer> getById(@PathVariable String id) {
        return customerRepository.findById(id)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Post
    public HttpResponse<Customer> save(@Body Customer data) {
        var saved = customerRepository.save(data);
        return HttpResponse.created(URI.create("/customers/" + saved.id()));
    }

    @Delete("/{id}")
    public HttpResponse<Void> deleteById(@PathVariable String id) {
        customerRepository.deleteById(id);
        return HttpResponse.noContent();
    }
}
