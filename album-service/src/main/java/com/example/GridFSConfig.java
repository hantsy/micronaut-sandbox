package com.example;

import com.mongodb.WriteConcern;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.gridfs.GridFSBucket;
import com.mongodb.reactivestreams.client.gridfs.GridFSBuckets;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

@Factory
public class GridFSConfig {

    @Bean
    GridFSBucket gridFSBucket(MongoClient client) {
        return GridFSBuckets.create(client.getDatabase("photos"))
                .withChunkSizeBytes(4096)
                //.withReadConcern(ReadConcern.MAJORITY)
                .withWriteConcern(WriteConcern.MAJORITY);
    }
}
