package com.example

import io.micronaut.data.repository.jpa.criteria.DeleteSpecification
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification
import io.micronaut.data.repository.jpa.criteria.QuerySpecification
import io.micronaut.data.repository.jpa.criteria.UpdateSpecification

object Specifications {

    fun titleLike(title: String): PredicateSpecification<Post> {
        return PredicateSpecification<Post> { root, criteriaBuilder ->
            criteriaBuilder.like(
                root.get("title"),
                "%$title%"
            )
        }
    }

    fun byKeyword(q: String): QuerySpecification<Post> {
        return QuerySpecification<Post> { root, _, criteriaBuilder ->
            criteriaBuilder.or(
                criteriaBuilder.like(root.get("title"), "%$q%"),
                criteriaBuilder.like(root.get("content"), "%$q%")
            )
        }
    }

    fun rejectAllPendingModerated(): UpdateSpecification<Post> {
        return UpdateSpecification<Post> {root, query, criteriaBuilder ->
            query.set(root.get("status"), Status.REJECTED)
            criteriaBuilder.equal(root.get<Status>("status"), Status.PENDING_MODERATED)
        }
    }

    fun removeAllRejected(): DeleteSpecification<Post> {
        return DeleteSpecification<Post> { root, _, criteriaBuilder ->
            criteriaBuilder.equal(root.get<Status>("status"), Status.REJECTED)
        }
    }

}