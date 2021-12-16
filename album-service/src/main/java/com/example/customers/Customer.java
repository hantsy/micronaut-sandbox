package com.example.customers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class Customer {
    private ObjectId id;
    private String name;
    private int age;
    private Address address;

    public static Customer of(String name, int age, Address address) {
        return Customer.of(null, name, age, address);
    }
}

