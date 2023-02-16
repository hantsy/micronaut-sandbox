package com.example.repository;

import com.example.domain.Post;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.jpa.repository.JpaSpecificationExecutor;
import lombok.RequiredArgsConstructor;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;

//@Repository()
//public interface PostRepository extends JpaRepository<Post, UUID>, JpaSpecificationExecutor<Post> {
//
//}

@Repository()
@RequiredArgsConstructor
public abstract class PostRepository implements JpaRepository<Post, UUID>, JpaSpecificationExecutor<Post> {
    private final EntityManager entityManager;

    public List<Post> findAllByTitleContains(String title) {
        return entityManager.createQuery("FROM Post AS p WHERE p.title like :title", Post.class)
                .setParameter("title", "%" + title + "%")
                .getResultList();
    }
}
