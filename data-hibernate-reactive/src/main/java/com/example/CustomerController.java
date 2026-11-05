package com.example;

import com.example.dto.CustomerDto;
import com.example.dto.NewCustomerCommand;
import com.example.dto.UpdateCustomerCommand;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

import static io.micronaut.http.HttpResponse.*;

@Controller("/customers")
@RequiredArgsConstructor
@Validated
public class CustomerController {
    private final CustomerRepository customerRepository;

    @Get
    HttpResponse<Flux<CustomerDto>> all() {
        return ok(customerRepository.findAll()
                .map(c -> new CustomerDto(c.getId(), c.getName(), c.getAge(), c.getAddress())));
    }

    @Get("{id}")
    Mono<MutableHttpResponse<CustomerDto>> byId(@PathVariable UUID id) {
        return customerRepository.findById(id)
                .map(c -> ok(new CustomerDto(c.getId(), c.getName(), c.getAge(), c.getAddress())))
                .switchIfEmpty(Mono.just(notFound()));
    }

    @Post
    Mono<HttpResponse<?>> save(@Body @Valid NewCustomerCommand cmd) {
        var customer = Customer.of(cmd.name(), cmd.age(), cmd.address());
        var saved = customerRepository.save(customer);
        return saved.map(s -> created(URI.create("/customers/" + s.getId())));
    }

    @Put("{id}")
    Mono<HttpResponse<?>> update(@PathVariable UUID id, @Body @Valid UpdateCustomerCommand cmd) {
        return customerRepository.findById(id)
                .flatMap(c -> {
                    c.setName(cmd.name());
                    c.setAge(cmd.age());
                    c.setAddress(cmd.address());
                    return customerRepository.update(c).then(Mono.<HttpResponse<?>>just(noContent()));
                })
                .switchIfEmpty(Mono.just(notFound()));
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
