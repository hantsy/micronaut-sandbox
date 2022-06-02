package com.example;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomCustomerRepository {
    @Transactional
    List<Customer> findAll();

    @Transactional
    Optional<Customer> findById(UUID id);

    UUID save(Customer data);

    Integer deleteAll();

    Integer deleteById(UUID id);
}
