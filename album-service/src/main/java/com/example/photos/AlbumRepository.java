package com.example.photos;


import com.mongodb.client.model.*;
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
public class AlbumRepository {
    private final MongoClient mongoClient;
    private final DefaultMongoConfiguration mongoConfiguration;

    public Flux<Album> findAll() {
        return Flux.from(albumsCollection().find());
    }

    public Mono<Album> findById(String id) {
        return Mono.from(albumsCollection().find(Filters.eq(id)));
    }

    public Mono<String> insertOne(Album data) {
        return Mono.from(albumsCollection().insertOne(data, new InsertOneOptions().bypassDocumentValidation(false)))
                .mapNotNull(result -> result.getInsertedId().asString().getValue());
    }

    public Mono<Long> update(String id, Album data) {
        return Mono.from(albumsCollection().replaceOne(Filters.eq(id), data, new ReplaceOptions().bypassDocumentValidation(true)))
                .mapNotNull(updateResult -> updateResult.getModifiedCount());
    }

    public Mono<Map<Integer, BsonValue>> insertMany(List<Album> data) {
        return Mono.from(albumsCollection().insertMany(data, new InsertManyOptions().bypassDocumentValidation(false).ordered(true)))
                .map(InsertManyResult::getInsertedIds);
    }

    public Mono<Long> deleteById(String id) {
        return Mono.from(albumsCollection().deleteOne(Filters.eq(id), new DeleteOptions()))
                .map(DeleteResult::getDeletedCount);
    }

    private MongoCollection<Album> albumsCollection() {
        var existingRegistries = new ArrayList<>(mongoConfiguration.getCodecRegistries());
        existingRegistries.add(fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        var codecRegistry = fromRegistries(existingRegistries);
        return mongoClient
                .getDatabase("photos")
                .getCollection("albums", Album.class)
                .withCodecRegistry(codecRegistry);
    }
}
