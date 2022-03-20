package com.example

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import jakarta.inject.Inject

import static io.micronaut.http.HttpResponse.*

@Controller("/customers")
class CustomerController {

    @Inject
    CustomerRepository customerRepository

    @Get
    def all() {
        ok(customerRepository.findAll())
    }

    @Get("/{id}")
    def getById(@PathVariable String id) {
        customerRepository.findById(id).map(data -> ok(data))
                .orElse(notFound())
    }

    @Post
    def save(Customer data) {
        def saved = customerRepository.save(data)
        created(URI.create("/customers/" + saved.id))
    }

    @Delete("/{id}")
    def deleteById(@PathVariable String id) {
        customerRepository.deleteById(id)
        noContent()
    }
}
