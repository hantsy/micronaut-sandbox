package com.example;

import com.example.application.CreatePostCommand;
import com.example.application.LatestComment;
import com.example.application.PaginatedResult;
import com.example.application.PostCommandService;
import com.example.application.PostDetails;
import com.example.application.PostQueryService;
import com.example.application.PostSummary;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.GenericArgument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.reactor.http.client.ReactorHttpClient;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@MicronautTest(environments = "mock", transactional = false)
class PostControllerTest {

    @MockBean(PostQueryService.class)
    PostQueryService mockedPostQueryService() {
        return mock(PostQueryService.class);
    }

    @MockBean(PostCommandService.class)
    PostCommandService mockedPostCommandService() {
        return mock(PostCommandService.class);
    }

    @Inject
    PostQueryService postQueryService;

    @Inject
    PostCommandService postCommandService;

    @Inject
    @Client("/posts")
    ReactorHttpClient client;

    @Test
    @DisplayName("find posts with pagination")
    public void testFindPosts() {
        PaginatedResult<PostSummary> expectedResult = new PaginatedResult<>(
                List.of(
                        new PostSummary(UUID.randomUUID(), "Test Post 1", 1L, Collections.emptyList()),
                        new PostSummary(UUID.randomUUID(), "Test Post 2", 2L, List.of("jOOQ", "Micronaut"))
                ),
                25L
        );
        when(this.postQueryService.findPostsByKeyword(anyString(), anyInt(), anyInt()))
                .thenReturn(expectedResult);

        var request = HttpRequest.GET("?q=test&offset=0&limit=10");
        client.exchange(request, new GenericArgument<PaginatedResult<PostSummary>>(){} )
                .as(StepVerifier::create)
                .consumeNextWith(res -> {
                    assertEquals(HttpStatus.OK, res.getStatus());
                    assertThat(res.body()).isEqualTo(expectedResult);
                })
                .verifyComplete();

        verify(this.postQueryService, times(1)).findPostsByKeyword("test", 0, 10);
        verifyNoMoreInteractions(this.postQueryService);
    }

    @Test
    @DisplayName("create a new post")
    public void testCreatePost() {
        UUID newPostId = UUID.randomUUID();
        when(this.postCommandService.createPost(any(CreatePostCommand.class)))
                .thenReturn(newPostId);

        CreatePostCommand command = new CreatePostCommand("New Post Title", "New Post Content", Collections.emptyList());
        var request = HttpRequest.POST("", command);
        client.exchange(request, Argument.VOID)
                .as(StepVerifier::create)
                .consumeNextWith(res -> {
                    assertEquals(HttpStatus.CREATED, res.getStatus());
                    assertThat(res.getHeaders().get("Location")).isEqualTo("/posts/" + newPostId);
                })
                .verifyComplete();

        verify(this.postCommandService, times(1)).createPost(command);
        verifyNoMoreInteractions(this.postCommandService);
    }

    @Test
    @DisplayName("get a post by id")
    public void testGetPostById() {
        UUID postId = UUID.randomUUID();
        PostDetails postDetails = new PostDetails(postId, "test blog", "test content", LocalDateTime.now(), LocalDateTime.now(), 1L, new LatestComment("test comment", LocalDateTime.now()));
        when(this.postQueryService.getPostDetailsById(any(UUID.class)))
                .thenReturn(postDetails);
        var request = HttpRequest.GET("/posts/" + postId);
        client.exchange(request, Argument.of(PostDetails.class))
                .as(StepVerifier::create)
                .consumeNextWith(res -> {
                    assertEquals(HttpStatus.OK, res.getStatus());
                    assertThat(res.body()).isEqualTo(postDetails);

                })
                .verifyComplete();

        verify(this.postQueryService, times(1)).getPostDetailsById(postId);
        verifyNoMoreInteractions(this.postQueryService);
    }
}
