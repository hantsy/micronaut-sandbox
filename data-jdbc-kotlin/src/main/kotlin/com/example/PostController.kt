package com.example

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.*
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.validation.Validated
import jakarta.validation.Valid
import java.net.URI
import java.util.*

@Controller("/posts")
@Validated
class PostController(private val posts: PostRepository) {

    @Get(uri = "/", produces = [MediaType.APPLICATION_JSON])
    fun all(): HttpResponse<List<PostDto>> = ok(posts.findAll().map { it.toDto() })

    @Get(uri = "/{id}", produces = [MediaType.APPLICATION_JSON])
    fun byId(@PathVariable id: UUID): HttpResponse<Any> {
        val post = posts.findById(id).orElse(null) ?: return notFound()
        return ok(post.toDto())
    }

    @io.micronaut.http.annotation.Post(consumes = [MediaType.APPLICATION_JSON])
    fun create(@Body @Valid body: NewPostCommand): HttpResponse<Any> {
        val post = Post(title = body.title, content = body.content)
        val saved = posts.save(post)
        return created(URI.create("/posts/" + saved.id))
    }

    @io.micronaut.http.annotation.Put(uri = "/{id}", consumes = [MediaType.APPLICATION_JSON])
    fun update(@PathVariable id: UUID, @Body @Valid body: UpdatePostCommand): HttpResponse<Any> {
        val post = posts.findById(id).orElse(null) ?: return notFound()
        val updated = post.copy(title = body.title, content = body.content)
        posts.update(updated)
        return noContent()
    }

    private fun Post.toDto() = PostDto(id!!, title, content, status, createdAt)
}
