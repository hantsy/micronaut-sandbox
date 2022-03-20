package com.example

import io.micronaut.data.mongodb.annotation.MongoRepository
import io.micronaut.data.repository.CrudRepository

@MongoRepository()
interface CustomerRepository extends CrudRepository<Customer, String> {
}