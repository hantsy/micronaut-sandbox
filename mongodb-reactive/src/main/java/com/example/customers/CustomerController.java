package com.example.customers;

import com.example.customers.dto.CustomerDto;
import com.example.customers.dto.NewCustomerCommand;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

import static io.micronaut.http.HttpResponse.*;

@Controller("/customers")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CustomerController {
    private final CustomerRepository customerRepository;

    @Get(uri = "/", produces = {MediaType.APPLICATION_JSON})
    public Flux<CustomerDto> all() {
        return this.customerRepository.findAll().map(this::toDto);
    }

    @Get(uri = "/{id}", produces = {MediaType.APPLICATION_JSON})
    public Mono<MutableHttpResponse<CustomerDto>> byId(@PathVariable ObjectId id) {
        return this.customerRepository.findById(id)
                .map(c -> ok(toDto(c)))
                .switchIfEmpty(Mono.just(notFound()));
    }

    @Post(uri = "/", consumes = {MediaType.APPLICATION_JSON})
    public Mono<HttpResponse<?>> create(@Body @Valid NewCustomerCommand cmd) {
        var customer = Customer.of(cmd.name(), cmd.age(), cmd.address());
        return this.customerRepository.insertOne(customer)
                .map(id -> created(URI.create("/customers/" + id.toHexString())));
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

    private CustomerDto toDto(Customer c) {
        return new CustomerDto(c.getId().toHexString(), c.getName(), c.getAge(), c.getAddress());
    }
}
