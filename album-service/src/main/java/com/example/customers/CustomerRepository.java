package com.example.customers;


import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.micronaut.configuration.mongo.core.DefaultMongoConfiguration;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonValue;
import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class CustomerRepository {
    private final MongoClient mongoClient;
    private final DefaultMongoConfiguration mongoConfiguration;

    public Flux<Customer> findAll() {
        return Flux.from(customersCollection().find());
    }

    public Mono<Customer> findById(ObjectId id) {
        return Mono.from(customersCollection().find(Filters.eq(id)));
    }

    public Mono<ObjectId> insertOne(Customer data) {
        return Mono.from(customersCollection().insertOne(data, new InsertOneOptions().bypassDocumentValidation(false)))
                .mapNotNull(result -> result.getInsertedId().asObjectId().getValue());
    }

    public Mono<Map<Integer, BsonValue>> insertMany(List<Customer> data) {
        return Mono.from(customersCollection().insertMany(data, new InsertManyOptions().bypassDocumentValidation(false).ordered(true)))
                .map(InsertManyResult::getInsertedIds);
    }

    public Mono<Long> deleteById(ObjectId id) {
        return Mono.from(customersCollection().deleteOne(Filters.eq(id), new DeleteOptions()))
                .map(DeleteResult::getDeletedCount);
    }

    public void init() {
        var people = List.of(
                Customer.of("Charles Babbage", 45, Address.of("5 Devonshire Street", "London", "W11")),
                Customer.of("Alan Turing", 28, Address.of("Bletchley Hall", "Bletchley Park", "MK12")),
                Customer.of("Timothy Berners-Lee", 61, Address.of("Colehill", "Wimborne", null))
        );
        Mono.from(customersCollection().drop())
                .then()
                .thenMany(this.insertMany(people))
                .subscribe(
                        result -> result.forEach((key, value) -> log.debug("saved key: {}, value: {}", key, value)),
                        error -> log.debug("initialization failed: {}", error),
                        () -> log.debug("done")
                );
    }

    public Mono<Long> deleteAll() {
        return Mono.from(customersCollection().deleteMany(Filters.empty(), new DeleteOptions()))
                .map(DeleteResult::getDeletedCount);
    }

    private MongoCollection<Customer> customersCollection() {
        return mongoClient
                .getDatabase("userdb")
                .getCollection("customers", Customer.class);
    }


}
