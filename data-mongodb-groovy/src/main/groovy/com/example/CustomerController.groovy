package com.example

import com.example.dto.CustomerDto
import com.example.dto.NewCustomerCommand
import com.example.dto.UpdateCustomerCommand
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.validation.Valid


@Controller("/customers")
@Validated
class CustomerController {

    @Inject
    CustomerRepository customerRepository

    @Get
    def all() {
        HttpResponse.ok(customerRepository.findAll().collect { c ->
            def dto = new CustomerDto()
            dto.id = c.id
            dto.name = c.name
            dto.age = c.age
            dto.address = c.address
            dto
        })
    }

    @Get("/{id}")
    def getById(@PathVariable String id) {
        customerRepository.findById(id).map { data ->
            def dto = new CustomerDto()
            dto.id = data.id
            dto.name = data.name
            dto.age = data.age
            dto.address = data.address
            HttpResponse.ok(dto)
        }.orElse(HttpResponse.notFound())
    }

    @Post
    def save(@Body @Valid NewCustomerCommand cmd) {
        def customer = Customer.of(cmd.name, cmd.age, cmd.address)
        def saved = customerRepository.save(customer)
        HttpResponse.created(URI.create("/customers/" + saved.id))
    }

    @Put("/{id}")
    def update(@PathVariable String id, @Body @Valid UpdateCustomerCommand cmd) {
        customerRepository.findById(id).map { existing ->
            existing.name = cmd.name
            existing.age = cmd.age
            existing.address = cmd.address
            customerRepository.update(existing)
            HttpResponse.noContent()
        }.orElse(HttpResponse.notFound())
    }

    @Delete("/{id}")
    def deleteById(@PathVariable String id) {
        customerRepository.deleteById(id)
        HttpResponse.noContent()
    }
}
