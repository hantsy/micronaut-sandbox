package com.example;

import io.micronaut.data.mongodb.annotation.MongoRepository;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;

import java.util.UUID;


@Repository
@MongoRepository
public interface CustomerRepository extends CrudRepository<Customer, String> {

    Page<Customer> findByAddressCityLike(String cityLike, PageRequest pageRequest);
}
