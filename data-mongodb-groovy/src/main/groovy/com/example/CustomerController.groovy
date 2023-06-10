package com.example

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import jakarta.inject.Inject


@Controller("/customers")
class CustomerController {

    @Inject
    CustomerRepository customerRepository

    @Get
    def all() {
        HttpResponse.ok(customerRepository.findAll())
    }

    @Get("/{id}")
    def getById(@PathVariable String id) {
        customerRepository.findById(id).map(data -> ok(data))
                .orElse(HttpResponse.notFound())
    }

    @Post
    def save(Customer data) {
        def saved = customerRepository.save(data)
        HttpResponse.created(URI.create("/customers/" + saved.id))
    }

    @Delete("/{id}")
    def deleteById(@PathVariable String id) {
        customerRepository.deleteById(id)
        HttpResponse.noContent()
    }
}
