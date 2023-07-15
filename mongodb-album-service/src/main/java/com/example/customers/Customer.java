package com.example.customers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@BsonDiscriminator(value = "albums")
public class Customer {
    @BsonId
    private ObjectId id;
    private String name;
    private int age;
    private Address address;

    public static Customer of(String name, int age, Address address) {
        return Customer.of(null, name, age, address);
    }
}

