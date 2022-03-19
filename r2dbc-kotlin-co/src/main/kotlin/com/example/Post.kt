package com.example

import io.micronaut.data.annotation.*
import io.micronaut.data.annotation.Relation.Cascade
import io.micronaut.data.model.naming.NamingStrategies
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.List

@MappedEntity(value = "posts", namingStrategy = NamingStrategies.UnderScoreSeparatedLowerCase::class)
data class Post(
    @AutoPopulated//generated value UUID does not work here.
    @field:Id var id: UUID? = null,
    var title: String,
    var content: String,
    var status: Status? = Status.DRAFT,
    @field:DateCreated var createdAt: LocalDateTime? = LocalDateTime.now(),
    @field:Relation(
        value = Relation.Kind.ONE_TO_MANY,
        mappedBy = "post",
        cascade = [Cascade.ALL]
    ) var comments: List<Comment> = emptyList<Comment>()
) {
    fun addComment(data: Comment) {
        data.post = this
        this.comments += data
    }
}