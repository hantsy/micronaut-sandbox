package com.example;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

@Singleton
@Requires(classes = {ExceptionHandler.class, PostNotFoundException.class})
public class RestExceptionHandler implements ExceptionHandler<PostNotFoundException, HttpResponse> {
    @Override
    public HttpResponse handle(HttpRequest request, PostNotFoundException exception) {
        return HttpResponse.notFound().body(exception.getMessage());
    }
}
