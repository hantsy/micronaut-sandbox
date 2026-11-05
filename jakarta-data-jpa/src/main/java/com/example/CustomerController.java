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

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static io.micronaut.http.HttpResponse.*;

@Controller("/customers")
@RequiredArgsConstructor
@Validated
public class CustomerController {
    private final CustomerRepository customerRepository;

    @Get
    HttpResponse<List<CustomerDto>> all() {
        return ok(customerRepository.findAll()
                .map(c -> new CustomerDto(c.getId(), c.getName(), c.getAge(), c.getAddress()))
                .toList());
    }

    @Get("{id}")
    MutableHttpResponse<CustomerDto> byId(@PathVariable UUID id) {
        return customerRepository.findById(id)
                .map(c -> ok(new CustomerDto(c.getId(), c.getName(), c.getAge(), c.getAddress())))
                .orElse(notFound());
    }

    @Post
    HttpResponse<?> save(@Body @Valid NewCustomerCommand cmd) {
        var customer = Customer.of(cmd.name(), cmd.age(), cmd.address());
        var saved = customerRepository.save(customer);
        return created(URI.create("/customers/" + saved));
    }

    @Put("{id}")
    HttpResponse<?> update(@PathVariable UUID id, @Body @Valid UpdateCustomerCommand cmd) {
        return customerRepository.findById(id)
                .map(c -> {
                    var updated = new Customer(c.getId(), cmd.name(), cmd.age(), cmd.address(), c.getVersion());
                    customerRepository.update(updated);
                    return noContent();
                })
                .orElse(notFound());
    }

    @Delete("{id}")
    MutableHttpResponse<Void> deleteById(@PathVariable UUID id) {
        customerRepository.deleteById(id);
        return noContent();
    }
}
