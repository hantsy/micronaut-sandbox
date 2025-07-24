package com.example;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

import static io.micronaut.data.annotation.Relation.Kind.EMBEDDED;

@Introspected
@Entity
@Table(name = "CUSTOMERS")
@Serdeable
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Customer{
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    UUID id;
    String name;
    Integer age;
    @Embedded  Address address;
    @Version Long version;

    public static Customer of(String name, Integer age, Address address) {
        return new Customer(null, name, age, address, null);
    }
}
