package com.example.client;

import com.example.controller.dto.CreatePostCommand;
import com.example.controller.dto.PostSummaryDto;
import io.micronaut.data.model.Page;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.client.annotation.Client;

import jakarta.validation.Valid;
import java.util.UUID;

@Client(value = "http://localhost:8080/posts")
@Header(name = "Content-Type", value = "application/json")
@Header(name = "Accept", value = "application/json")
public interface PostServiceClient {

    @Get(uri = "/", produces = MediaType.APPLICATION_JSON)
    HttpResponse<PageDto<PostSummaryDto>> getAll(@QueryValue(defaultValue = "") String q,
                                              @QueryValue(defaultValue = "") String status,
                                              @QueryValue(defaultValue = "0") int page,
                                              @QueryValue(defaultValue = "10") int size);

    @io.micronaut.http.annotation.Post(uri = "/", consumes = MediaType.APPLICATION_JSON)
    HttpResponse<Void> create(@Body @Valid CreatePostCommand dto);

    @Get(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
    HttpResponse<com.example.domain.Post> getById(@PathVariable UUID id);

}
