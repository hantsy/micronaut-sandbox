package com.example;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static io.micronaut.http.HttpResponse.*;

@Controller("/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerRepository customerRepository;

    @Get
    HttpResponse<List<Customer>> all() {
        return ok(customerRepository.findAll().toList());
    }

    @Get("{id}")
    MutableHttpResponse<Customer> byId(@PathVariable String id) {
        return customerRepository.findById(id)
                .map(HttpResponse::ok)
                .orElse(notFound());
    }

    @Post
    HttpResponse<?> save(Customer customer) {
        var saved = customerRepository.save(customer);
        return created(URI.create("/customers/" + saved));
    }

    @Delete("{id}")
    MutableHttpResponse<Void> deleteById(@PathVariable String id) {
        customerRepository.deleteById(id);
        return noContent();
    }
}
