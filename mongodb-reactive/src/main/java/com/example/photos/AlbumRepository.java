package com.example.photos;


import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.micronaut.configuration.mongo.core.DefaultMongoConfiguration;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonValue;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    public Mono<Album> findById(ObjectId id) {
        return Mono.from(albumsCollection().find(Filters.eq(id)));
    }

    public Mono<ObjectId> insertOne(Album data) {
        return Mono.from(albumsCollection().insertOne(data, new InsertOneOptions().bypassDocumentValidation(false)))
                .mapNotNull(result -> result.getInsertedId().asObjectId().getValue());
    }

    public Mono<Long> update(ObjectId id, Album data) {
        return Mono.from(albumsCollection().replaceOne(Filters.eq(id), data, new ReplaceOptions().bypassDocumentValidation(true)))
                .mapNotNull(UpdateResult::getModifiedCount);
    }

    public Mono<Map<Integer, BsonValue>> insertMany(List<Album> data) {
        return Mono.from(albumsCollection().insertMany(data, new InsertManyOptions().bypassDocumentValidation(false).ordered(true)))
                .map(InsertManyResult::getInsertedIds);
    }

    public Mono<Long> deleteById(ObjectId id) {
        return Mono.from(albumsCollection().deleteOne(Filters.eq(id), new DeleteOptions()))
                .map(DeleteResult::getDeletedCount);
    }

    public Mono<Long> deleteAll() {
        return Mono.from(albumsCollection().deleteMany(Filters.empty(), new DeleteOptions()))
                .map(DeleteResult::getDeletedCount);
    }

    private MongoCollection<Album> albumsCollection() {
        var providers = fromProviders(
                //CodecRegistries.fromCodecs(new StringObjectIdCodec()),
                mongoConfiguration.getClientSettings().build().getCodecRegistry(),
                PojoCodecProvider.builder()
                        .register("com.example.photos")
                        .register(new OptionalPropertyCodecProvider())
                        .conventions(Conventions.DEFAULT_CONVENTIONS)
                        .build());
        var codecRegistry = fromRegistries(List.of(providers));
        return mongoClient
                .getDatabase("photos")
                .getCollection("albums", Album.class)
                .withCodecRegistry(codecRegistry);
    }
}
