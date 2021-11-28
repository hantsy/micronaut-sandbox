package com.example;

import com.example.client.PostServiceClient;
import com.example.domain.Post;
import com.example.repository.CommentRepository;
import com.example.repository.PostRepository;
import io.micronaut.context.env.Environment;
import io.micronaut.data.jpa.repository.criteria.Specification;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@MicronautTest(environments = Environment.TEST)
public class PostServiceClientTest {

    @Inject
    PostServiceClient client;

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
//        var content = List.of(Post.builder().id(UUID.randomUUID()).title("test title").content("test content").build());
//        when(this.posts.findAll(isA(Specification.class), isA(Pageable.class))).thenReturn(
//                Page.of(content, Pageable.from(0, 20), 1)
//        );
//
//        var response = client.getAll("", "", 0, 10);
//        assertEquals(HttpStatus.OK, response.status());
//        var body = response.body();
//        assertThat(body.getTotalSize()).isEqualTo(1);
//        assertThat(body.getContent().get(0).title()).isEqualTo("test title");
//
//        verify(this.posts, times(1)).findAll(isA(Specification.class), isA(Pageable.class));
//        verifyNoMoreInteractions(this.posts);
//    }

    @Test
    @DisplayName("test GET '/posts/{id}' endpoint")
    public void testGetSinglePost() throws Exception {
        when(this.posts.findById(any(UUID.class))).thenReturn(
                Optional.ofNullable(Post.builder().id(UUID.randomUUID()).title("test title").content("test content").build())
        );

        var response = client.getById(UUID.randomUUID());
        assertEquals(HttpStatus.OK, response.status());
        var body = response.body();
        assertThat(body.getTitle()).isEqualTo("test title");

        verify(this.posts, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    @DisplayName("test GET '/posts/{id}' endpoint that does not exist")
    public void testGetSinglePost_notFound() throws Exception {
        when(this.posts.findById(any(UUID.class))).thenReturn(Optional.ofNullable(null));

        var response = client.getById(UUID.randomUUID());
        assertEquals(HttpStatus.NOT_FOUND, response.status());

        verify(this.posts, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(this.posts);
    }
}

