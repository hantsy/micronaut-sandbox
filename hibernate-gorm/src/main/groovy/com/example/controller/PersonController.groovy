package com.example.controller

import com.example.model.Person
import com.example.service.PersonService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import jakarta.inject.Inject

import static io.micronaut.http.HttpResponse.ok

@Controller("/persons")
class PersonController {
    @Inject
    PersonService personService;

    @Get
    def all() {
        return ok(Person.getAll())
    }

    @Get("/{id}")
    def getById(@PathVariable UUID id) {
        return ok(Person.get(id))
    }

    @Get("/search")
    def searchByName(@QueryValue String name) {
        ok(personService.findByNickNames(name))
    }
}
