package com.example;

import com.example.domain.model.Post;
import com.example.domain.model.Status;
import com.example.domain.repository.PostRepository;
import io.micronaut.context.env.Environment;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest(environments = Environment.TEST, startApplication = false)
class PostRepositoryTest {

    @Inject
    PostRepository postRepository;

    @Test
    void testSavePost() {
        // Create a new Post instance
        Post newPost = new Post(null, "Test Title", "Test Content", Status.DRAFT, null, null, null);

        // Save the post
        Post savedPost = postRepository.save(newPost);

        // Verify the post was saved
        assertThat(savedPost).isNotNull();
        assertThat(savedPost.id()).isNotNull();
        assertThat(savedPost.title()).isEqualTo("Test Title");
        assertThat(savedPost.content()).isEqualTo("Test Content");
        assertThat(savedPost.status()).isEqualTo(Status.DRAFT);
        assertThat(savedPost.createdAt()).isNotNull();
        assertThat(savedPost.updatedAt()).isNotNull();
        assertThat(savedPost.version()).isEqualTo(0L);

        // Optionally, retrieve the post to confirm it exists in the database
        Post foundPost = postRepository.findById(savedPost.id()).orElse(null);
        assertThat(foundPost).isNotNull();
        assertThat(foundPost.id()).isEqualTo(savedPost.id());
        assertThat(foundPost.title()).isEqualTo(savedPost.title());
    }
}
