package com.example.repository;

import com.example.domain.Post;
import com.example.domain.Post_;
import com.example.domain.Status;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.jpa.repository.criteria.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class PostSpecifications {
    private PostSpecifications(){
        // forbid to instantiate
    }

    public static Specification<Post> filterByKeywordAndStatus(
            final String keyword,
            final Status status
    ) {
        return (Root<Post> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                predicates.add(
                        cb.or(
                                cb.like(root.get(Post_.title), "%" + keyword + "%"),
                                cb.like(root.get(Post_.content), "%" + keyword + "%")
                        )
                );
            }

            if (status != null) {
                predicates.add(cb.equal(root.get(Post_.status), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
