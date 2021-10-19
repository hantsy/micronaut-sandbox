package com.example

import io.micronaut.data.annotation.*
import io.micronaut.data.model.naming.NamingStrategies
import java.time.LocalDateTime
import java.util.*

@MappedEntity(value = "posts", namingStrategy = NamingStrategies.UnderScoreSeparatedLowerCase::class)
data class Post(
    @field:Id @field:GeneratedValue(GeneratedValue.Type.UUID) var id: UUID? = null,
    var title: String,
    var content: String,
    var status: Status? = Status.DRAFT,
    @field:DateCreated var createdAt: LocalDateTime? = LocalDateTime.now()
)