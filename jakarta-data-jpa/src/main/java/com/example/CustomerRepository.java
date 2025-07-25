package com.example;

import jakarta.data.Order;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface CustomerRepository extends CrudRepository<Customer, UUID> {

    Optional<Customer> findByName(String name);

    Page<Customer> findByAddressCityLike(String cityLike, PageRequest pageRequest);

    List<Customer> findByAddressZip(String zip, Order<Customer> order);

    @Query("where name like :name")
    @OrderBy("name")
    Customer[] byNameLike(@Param("name") String customerName);
}
