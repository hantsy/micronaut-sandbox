package com.example;

import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.TypeConverter;
import jakarta.inject.Singleton;
import org.bson.types.ObjectId;

import java.util.Optional;

@Singleton
public class StringToObjectIdConverter implements TypeConverter<String, ObjectId> {

    @Override
    public Optional<ObjectId> convert(String object, Class<ObjectId> targetType, ConversionContext context) {
        return Optional.of(new ObjectId(object));
    }
}
