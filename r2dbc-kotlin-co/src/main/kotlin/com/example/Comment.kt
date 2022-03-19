package com.example

import com.fasterxml.jackson.annotation.JsonIgnore
import io.micronaut.data.annotation.*
import io.micronaut.data.model.naming.NamingStrategies
import java.time.LocalDateTime
import java.util.*

@MappedEntity(value = "comments", namingStrategy = NamingStrategies.UnderScoreSeparatedLowerCase::class)
data class Comment(
    @AutoPopulated//generated value UUID does not work here.
    @field:Id var id: UUID? = null,
    var content: String,
    @field:DateCreated var createdAt: LocalDateTime? = LocalDateTime.now(),
    @field:Relation(Relation.Kind.MANY_TO_ONE) @field:MappedProperty("post_id") @field:JsonIgnore var post: Post? = null
)