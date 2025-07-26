package com.example;

import jakarta.data.repository.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static jakarta.data.repository.By.ID;

@Repository
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

    @Delete
    void delete(Customer data);

//    @Query("""
//    DELETE FROM Customer
//    """)
//    @Transactional
//    void deleteAll();
//
//    @Query("""
//    DELETE FROM Customer
//    WHERE id=:id
//    """)
//    @Transactional
//    void deleteById(@Param("id") UUID id);

//    see: https://github.com/micronaut-projects/micronaut-data/issues/3490
//    StatelessSession session();
//
//    default void cleanup() {
//        session().createMutationQuery("delete from Customer").executeUpdate();
//    }
}
