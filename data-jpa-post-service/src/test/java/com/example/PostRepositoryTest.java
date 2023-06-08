package com.example;

import com.example.domain.Post;
import com.example.repository.PostRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(application = Application.class, startApplication = false)
class PostRepositoryTest {

    @Inject
    PostRepository posts;

    @PersistenceContext
    EntityManager entityManager;

    @Test
    void testCreatePost() {
        var entity = Post.builder().title("test title").content("test content").build();
        this.entityManager.persist(entity);

        assertThat(entity.getId()).isNotNull();
        assertTrue(posts.findById(entity.getId()).isPresent());
    }

}
