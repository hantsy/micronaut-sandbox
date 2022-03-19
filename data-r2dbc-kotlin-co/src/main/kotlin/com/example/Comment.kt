package com.example

import com.fasterxml.jackson.annotation.JsonIgnore
import io.micronaut.core.annotation.Introspected
import io.micronaut.data.annotation.*
import io.micronaut.data.model.naming.NamingStrategies
import jakarta.persistence.JoinColumn
import java.time.LocalDateTime
import java.util.*

@Introspected
@MappedEntity(value = "comments", namingStrategy = NamingStrategies.UnderScoreSeparatedLowerCase::class)
data class Comment(
    @AutoPopulated//generated value UUID does not work here.
    @field:Id val id: UUID?,

    val content: String,

    @field:DateCreated var createdAt: LocalDateTime?,

    @field:Relation(value = Relation.Kind.MANY_TO_ONE) @field:JoinColumn(name ="post_id") @field:JsonIgnore
    @field:Join(value = "post")
    var post: Post? = null
) {
    constructor(content: String) : this(null, content, null, null)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Comment

        if (content != other.content) return false

        return true
    }

    override fun hashCode(): Int {
        return content.hashCode()
    }

    override fun toString(): String {
        return "Comment(id=$id, content='$content', createdAt=$createdAt, post=${post?.id})"
    }

}