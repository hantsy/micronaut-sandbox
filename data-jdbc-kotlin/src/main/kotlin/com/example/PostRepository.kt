package com.example

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.repository.PageableRepository
import io.micronaut.data.repository.jpa.JpaSpecificationExecutor
import java.util.*

@JdbcRepository
interface PostRepository : PageableRepository<Post, UUID>, JpaSpecificationExecutor<Post>