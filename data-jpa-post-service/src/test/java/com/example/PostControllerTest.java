package com.example;

import com.example.controller.dto.PostDetailsDto;
import com.example.domain.Post;
import com.example.repository.CommentRepository;
import com.example.repository.PostRepository;
import io.micronaut.data.jpa.repository.criteria.Specification;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.path.json.JsonPath;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@MicronautTest(environments = "mock")
public class PostControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    PostRepository posts;

    @Inject
    CommentRepository comments;

    @MockBean(PostRepository.class)
    PostRepository posts() {
        return mock(PostRepository.class);
    }

    @MockBean(CommentRepository.class)
    CommentRepository comments() {
        return mock(CommentRepository.class);
    }

//    @Test
//    @DisplayName("test GET '/posts' endpoint")
//    public void testGetAllPosts() throws Exception {
//        when(this.posts.findAll()).thenReturn(
//                List.of(Post.builder().id(UUID.randomUUID()).title("test title").content("test content").build())
//        );
//        var response = client.toBlocking().exchange("/posts", PostSummaryDto[].class);
//        assertEquals(HttpStatus.OK, response.status());
//        var body = response.body();
//        assertThat(body.length).isEqualTo(1);
//        assertThat(body[0].title()).isEqualTo("test title");
//
//        verify(this.posts, times(1)).findAll();
//        verifyNoMoreInteractions(this.posts);
//    }

    @Test
    @DisplayName("test GET '/posts' endpoint")
    public void testGetAllPosts() throws Exception {
        var content = List.of(Post.builder().id(UUID.randomUUID()).title("test title").content("test content").build());
        when(this.posts.findAll(isA(Specification.class), isA(Pageable.class))).thenReturn(
                Page.of(content, Pageable.from(0, 20), 1L)
        );
        var request = HttpRequest.GET("/posts");
        var response = client.toBlocking().exchange(request, String.class);
        assertEquals(HttpStatus.OK, response.status());
        var body = response.body();
        assertThat(JsonPath.from(body).getInt("totalSize")).isEqualTo(1);
        assertThat(JsonPath.from(body).getString("content[0].title")).isEqualTo("test title");

        verify(this.posts, times(1)).findAll(isA(Specification.class), isA(Pageable.class));
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    @DisplayName("test GET '/posts/{id}' endpoint")
    public void testGetSinglePost() throws Exception {
        when(this.posts.findById(any(UUID.class))).thenReturn(
                Optional.ofNullable(Post.builder().id(UUID.randomUUID()).title("test title").content("test content").build())
        );
        var request = HttpRequest.GET(UriBuilder.of("/posts/{id}").expand(Map.of("id", UUID.randomUUID())));
        var response = client.toBlocking().exchange(request, PostDetailsDto.class);
        assertEquals(HttpStatus.OK, response.status());
        var body = response.body();
        assertThat(body.title()).isEqualTo("test title");

        verify(this.posts, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    @DisplayName("test GET '/posts/{id}' endpoint that does not exist")
    public void testGetSinglePost_notFound() throws Exception {
        when(this.posts.findById(any(UUID.class))).thenReturn(Optional.ofNullable(null));
        var request = HttpRequest.GET(UriBuilder.of("/posts/{id}").expand(Map.of("id", UUID.randomUUID())));
        var exception = assertThrows(HttpClientResponseException.class, () -> client.toBlocking().exchange(request, PostDetailsDto.class));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(this.posts, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(this.posts);
    }
}

