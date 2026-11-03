package com.example.controller;

import io.micronaut.data.model.Page;
import jakarta.inject.Singleton;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;

@Singleton
public class PageJsonSerializer extends ValueSerializer<Page<?>> {
    @Override
    public void serialize(Page<?> value, JsonGenerator gen, SerializationContext ctxt) {
        gen.writeStartObject();
        gen.writeNumberProperty("pageNumber", value.getPageNumber());
        if (value.getNumberOfElements() != value.getSize()) {
            //only display it in the last page when number of elements is not equal to page size.
            gen.writeNumberProperty("numberOfElements", value.getNumberOfElements());
        }
        gen.writeNumberProperty("size", value.getSize());
        gen.writeNumberProperty("totalPages", value.getTotalPages());
        gen.writeNumberProperty("totalSize", value.getTotalSize());
        gen.writeName("content");
        ctxt.writeValue(gen, value.getContent());
        gen.writeEndObject();
    }
}
