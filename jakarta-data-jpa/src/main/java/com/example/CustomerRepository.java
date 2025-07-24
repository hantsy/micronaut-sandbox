package com.example;

import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;

import java.util.UUID;


@Repository
public interface CustomerRepository extends CrudRepository<Customer, UUID> {

    Page<Customer> findByAddressCityLike(String cityLike, PageRequest pageRequest);
}
