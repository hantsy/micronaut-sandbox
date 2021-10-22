package com.example.persons;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class Person {
    private ObjectId id;
    private String name;
    private int age;
    private Address address;

    public static Person of(String name, int age, Address address) {
        return Person.of(null, name, age, address);
    }
}

