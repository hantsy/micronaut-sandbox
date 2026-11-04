package com.example;

import io.micronaut.context.annotation.Primary;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.Encoder;
import io.micronaut.serde.Serializer;
import jakarta.inject.Singleton;
import org.bson.types.ObjectId;

import java.io.IOException;

// micronaut-serde-bson registers a BSON codec serde for ObjectId that fails on the JSON
// encoder, so this bean has to win the lookup for the HTTP JSON mapper.
@Primary
@Singleton
public class ObjectIdJsonSerializer implements Serializer<ObjectId> {
    @Override
    public void serialize(Encoder encoder, EncoderContext context, Argument<? extends ObjectId> type, ObjectId value)
            throws IOException {
        encoder.encodeString(value.toHexString());
    }
}
