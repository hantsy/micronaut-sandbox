package com.example;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerDao {
    List<Customer> findAll();

    Optional<Customer> findById(UUID id);

    UUID save(Customer data);

    Integer deleteAll();

    Integer deleteById(UUID id);
}
