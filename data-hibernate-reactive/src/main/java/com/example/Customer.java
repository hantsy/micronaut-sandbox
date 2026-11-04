package com.example;

import lombok.*;
import io.micronaut.serde.annotation.Serdeable;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Serdeable
public class Customer implements Serializable {
    @Id
    @GeneratedValue
    private UUID id ;

    private String name;

    private int age;

    @Embedded
    private Address address;

    @Version
    private Long version;

    static Customer of(String name, int age, Address address) {
        Customer customer = new Customer();
        customer.name = name;
        customer.age = age;
        customer.address = address;
        return customer;
    }
}
