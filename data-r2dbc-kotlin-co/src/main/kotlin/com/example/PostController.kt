package com.example

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.*
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.validation.Validated
import kotlinx.coroutines.flow.Flow
import java.net.URI
import java.util.*
import jakarta.validation.Valid

@Controller("/posts")
@Validated
class PostController(private val posts: PostRepository) {

    @Get(uri = "/", produces = [MediaType.APPLICATION_JSON])
    fun all(): HttpResponse<Flow<Post>> = ok(posts.findAll())

    @Get(uri = "/{id}", produces = [MediaType.APPLICATION_JSON])
    suspend fun byId(@PathVariable id: UUID): HttpResponse<Any> {
        val post = posts.findById(id) ?: return notFound()
        return ok(post)
    }

    @io.micronaut.http.annotation.Post(consumes = [MediaType.APPLICATION_JSON])
    suspend fun create(@Body @Valid body: CreatePostCommand): HttpResponse<Any> {
        val data = Post(title = body.title, content = body.content)
        val saved = posts.save(data)
        return created(URI.create("/posts/" + saved.id))
    }
}