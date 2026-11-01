package com.example;

import jakarta.inject.Singleton;
import org.bson.types.ObjectId;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;

@Singleton
public class ObjectIdJsonSerializer extends ValueSerializer<ObjectId> {
    @Override
    public void serialize(ObjectId value, JsonGenerator gen, SerializationContext ctxt) {
        gen.writeString(value.toHexString());
    }
}
