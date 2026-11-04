package com.example

import io.micronaut.core.annotation.Introspected
import io.micronaut.data.annotation.*
import io.micronaut.data.annotation.Relation.Cascade
import io.micronaut.data.model.naming.NamingStrategies
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDateTime
import java.util.*

@Serdeable
@Introspected
@MappedEntity(value = "posts", namingStrategy = NamingStrategies.UnderScoreSeparatedLowerCase::class)
data class Post(
    @AutoPopulated//generated value UUID does not work here.
    @field:Id
    @field:MappedProperty(definition = "UUID DEFAULT gen_random_uuid()")
    val id: UUID?,
    val title: String,
    val content: String,
    val status: Status? = Status.DRAFT,
    @field:DateCreated
    @field:MappedProperty(definition = "TIMESTAMP DEFAULT LOCALTIMESTAMP")
    val createdAt: LocalDateTime?,
    @field:Relation(
        value = Relation.Kind.ONE_TO_MANY,
        mappedBy = "post",
        cascade = [Cascade.ALL]
    ) var comments: List<Comment>?
) {
    constructor(title: String, content: String) : this(null, title, content, Status.DRAFT, null, emptyList())

    fun addComment(data: Comment) {
        data.post = this
        if (this.comments == null) this.comments = emptyList()
        this.comments = this.comments?.plus(data)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Post

        if (title != other.title) return false

        return true
    }

    override fun hashCode(): Int {
        return title.hashCode()
    }

    override fun toString(): String {
        return "Post(id=$id, title='$title', content='$content', status=$status, createdAt=$createdAt, comments=$comments)"
    }

}