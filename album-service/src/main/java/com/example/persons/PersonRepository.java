package com.example.persons;


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
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class PersonRepository {
    private final MongoClient mongoClient;
    private final DefaultMongoConfiguration mongoConfiguration;

    public Flux<Person> findAll() {
        return Flux.from(persons().find());
    }

    public Mono<Person> findById(ObjectId id) {
        return Mono.from(persons().find(Filters.eq(id)));
    }

    public Mono<ObjectId> insertOne(Person data) {
        return Mono.from(persons().insertOne(data, new InsertOneOptions().bypassDocumentValidation(false)))
                .mapNotNull(result -> result.getInsertedId().asObjectId().getValue());
    }

    public Mono<Map<Integer, BsonValue>> insertMany(List<Person> data) {
        return Mono.from(persons().insertMany(data, new InsertManyOptions().bypassDocumentValidation(false).ordered(true)))
                .map(InsertManyResult::getInsertedIds);
    }

    public Mono<Long> deleteById(ObjectId id) {
        return Mono.from(persons().deleteOne(Filters.eq(id), new DeleteOptions()))
                .map(DeleteResult::getDeletedCount);
    }

    public void init() {
        var people = List.of(
                Person.of("Charles Babbage", 45, Address.of("5 Devonshire Street", "London", "W11")),
                Person.of("Alan Turing", 28, Address.of("Bletchley Hall", "Bletchley Park", "MK12")),
                Person.of("Timothy Berners-Lee", 61, Address.of("Colehill", "Wimborne", null))
        );
        Mono.from(persons().drop())
                .then()
                .thenMany(this.insertMany(people))
                .subscribe(
                        result -> result.forEach((key, value) -> log.debug("saved key: {}, value: {}", key, value)),
                        error -> log.debug("initialization failed: {}", error),
                        () -> log.debug("done")
                );
    }

    private MongoCollection<Person> persons() {
//        var existingRegistries = new ArrayList<>(mongoConfiguration.getCodecRegistries());
//        existingRegistries.add(fromProviders(PojoCodecProvider.builder().automatic(true).build()));
//        var codecRegistry = fromRegistries(existingRegistries);
        return mongoClient
                .getDatabase("photos")
                .getCollection("persons", Person.class);
        //.withCodecRegistry(codecRegistry);
    }
}
