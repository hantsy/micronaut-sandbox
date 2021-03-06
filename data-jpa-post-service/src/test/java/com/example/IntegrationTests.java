package com.example;

import com.example.controller.dto.CommentDetailsDto;
import com.example.controller.dto.CreateCommentCommand;
import com.example.controller.dto.CreatePostCommand;
import com.example.domain.Post;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.path.json.JsonPath;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest
@Slf4j
class IntegrationTests {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    EmbeddedApplication<?> application;

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }

    @Test
    void testGetAllPosts() {
        var response = client.exchange(HttpRequest.GET("/posts"), String.class);

        var bodyFlux = Flux.from(response).map(HttpResponse::body);
        StepVerifier.create(bodyFlux)
                .consumeNextWith(posts -> assertThat(JsonPath.from(posts).getInt("totalSize")).isGreaterThanOrEqualTo(2))
                .verifyComplete();
    }

    @Test
    public void testCrudFlow() {
        //create a new post
        var request = HttpRequest.POST("/posts", new CreatePostCommand("test title", "test content"));
        var blockingHttpClient = client.toBlocking();
        var response = blockingHttpClient.exchange(request);
        assertThat(response.status().getCode()).isEqualTo(201);
        var savedUrl = response.getHeaders().get("Location");
        assertThat(savedUrl).isNotNull();
        log.debug("saved post url: {}", savedUrl);

        //get by id
        var getPostResponse = blockingHttpClient.exchange(savedUrl, Post.class);
        assertThat(getPostResponse.getStatus().getCode()).isEqualTo(200);

        // add comments
        var addCommentRequest = HttpRequest.POST(savedUrl + "/comments", new CreateCommentCommand("test content"));
        var addCommentResponse = blockingHttpClient.exchange(addCommentRequest);
        assertThat(addCommentResponse.getStatus().getCode()).isEqualTo(201);
        var savedCommentUrl = addCommentResponse.getHeaders().get("Location");
        assertThat(savedCommentUrl).isNotNull();

        // get all comments
        var getAllCommentsRequest = HttpRequest.GET(savedUrl + "/comments");
        var getAllCommentsResponse = blockingHttpClient.exchange(getAllCommentsRequest, Argument.listOf(CommentDetailsDto.class));
        assertThat(getAllCommentsResponse.status().getCode()).isEqualTo(200);
        assertThat(getAllCommentsResponse.body().size()).isEqualTo(1);

        //delete by id
        var deletePostResponse = blockingHttpClient.exchange(HttpRequest.DELETE(savedUrl));
        assertThat(deletePostResponse.getStatus().getCode()).isEqualTo(204);

        //get by id again(404)
        var e = Assertions.assertThrows(HttpClientResponseException.class, () ->
                blockingHttpClient.exchange(HttpRequest.GET(savedUrl)));
        var getPostResponse2 = e.getResponse();
        assertThat(getPostResponse2.getStatus().getCode()).isEqualTo(404);
    }

}
