package com.example.controller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.micronaut.data.model.Page;
import jakarta.inject.Singleton;

import java.io.IOException;

@Singleton
public class PageJsonSerializer extends JsonSerializer<Page<?>> {
    @Override
    public void serialize(Page<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("pageNumber", value.getPageNumber());
        if (value.getNumberOfElements() != value.getSize()) {
            //only display it in the last page when number of elements is not equal to page size.
            gen.writeNumberField("numberOfElements", value.getNumberOfElements());
        }
        gen.writeNumberField("size", value.getSize());
        gen.writeNumberField("totalPages", value.getTotalPages());
        gen.writeNumberField("totalSize", value.getTotalSize());
        gen.writeObjectField("content", value.getContent());
        gen.writeEndObject();
    }
}
