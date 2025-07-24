package com.example;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import jakarta.data.Limit;
import jakarta.data.Sort;
import jakarta.data.repository.*;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static jakarta.data.repository.By.*;

@Repository
@JdbcRepository(dialect = Dialect.POSTGRES)
public interface CustomerDao {
    @Find
    @OrderBy("name")
    List<Customer> findAll();

    @Find
    Optional<Customer> findById(@By(ID) UUID id);

//    @Find
//    List<Customer> findByCity(@By("address.city") String city, Limit limit, Sort<?>... sort);

    @Insert
    Customer save(Customer data);

    @Update
    void update(Customer data);

//    @Query("""
//    DELETE FROM CUSTOMERS
//    """)
//    @Transactional
//    void deleteAll();
//
//    @Query("""
//    DELETE FROM CUSTOMERS WHERE ID=:id
//    """)
//    @Transactional
//    void deleteById(@Param("id") UUID id);
}
