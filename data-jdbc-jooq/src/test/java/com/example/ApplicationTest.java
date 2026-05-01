package com.example;

import com.example.application.CreatePostCommand;
import com.example.application.PaginatedResult;
import com.example.application.PostDetails;
import com.example.application.PostSummary;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.GenericArgument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
class ApplicationTest {

    @Inject
    EmbeddedApplication<?> application;

    @Inject
    @Client("/posts")
    HttpClient postClient; // Changed to blocking HttpClient

    @Test
    void testItWorks() {
        assertTrue(application.isRunning());
    }

    @Test
    void testPostCrudOperations() {
        // 1. Get initial count of all posts
        HttpRequest<?> initialGetAllRequest = HttpRequest.GET("/posts");
        HttpResponse<PaginatedResult<PostSummary>> initialGetAllResponse = postClient.toBlocking()
                .exchange(initialGetAllRequest, new GenericArgument<PaginatedResult<PostSummary>>() {});
        assertEquals(HttpStatus.OK, initialGetAllResponse.getStatus());
        PaginatedResult<PostSummary> initialPosts = initialGetAllResponse.body();
        assertNotNull(initialPosts);
        long initialCount = initialPosts.count();

        // 2. Create a new post
        CreatePostCommand createCommand = new CreatePostCommand("Test Post Title", "Test Post Content", Collections.emptyList());
        HttpRequest<?> createRequest = HttpRequest.POST("/posts", createCommand);
        HttpResponse<?> createResponse = postClient.toBlocking().exchange(createRequest);
        assertEquals(HttpStatus.CREATED, createResponse.getStatus());
        String location = createResponse.getHeaders().get("Location");
        assertNotNull(location);
        UUID newPostId = UUID.fromString(location.substring(location.lastIndexOf('/') + 1));

        // 3. Get the newly created post by ID
        HttpRequest<?> getByIdRequest = HttpRequest.GET(UriBuilder.of("/posts/{id}").expand(Map.of("id", newPostId)));
        HttpResponse<PostDetails> getByIdResponse = postClient.toBlocking().exchange(getByIdRequest, PostDetails.class);
        assertEquals(HttpStatus.OK, getByIdResponse.getStatus());
        PostDetails newPost = getByIdResponse.body();
        assertNotNull(newPost);
        assertEquals(newPostId, newPost.id());
        assertEquals("Test Post Title", newPost.title());
        assertEquals("Test Post Content", newPost.content());

        // 4. Verify total count increased by one
        HttpResponse<PaginatedResult<PostSummary>> finalGetAllResponse = postClient.toBlocking()
                .exchange(initialGetAllRequest, new GenericArgument<PaginatedResult<PostSummary>>() {});
        assertEquals(HttpStatus.OK, finalGetAllResponse.getStatus());
        PaginatedResult<PostSummary> finalPosts = finalGetAllResponse.body();
        assertNotNull(finalPosts);
        assertEquals(initialCount + 1, finalPosts.count());

        // Optional: Clean up the created post (if a delete endpoint existed)
        // For now, we'll just assert its presence
        assertThat(finalPosts.data()).anyMatch(p -> p.id().equals(newPostId));
    }
}
