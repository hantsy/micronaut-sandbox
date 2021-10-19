package com.example

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.repository.PageableRepository
import java.util.*

@JdbcRepository
interface PostRepository : PageableRepository<Post, UUID>