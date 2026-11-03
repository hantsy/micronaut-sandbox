package com.example.controller;

import com.example.BlogProperties;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

import static io.micronaut.http.HttpResponse.ok;

@Controller("/info")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class InfoController {

    private final BlogProperties properties;

    @Get("/")
    public HttpResponse<?> info() {
        return ok(this.properties);
    }
}
