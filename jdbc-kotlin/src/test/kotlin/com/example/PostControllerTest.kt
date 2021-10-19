package com.example

import io.kotest.matchers.shouldBe
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.inject.Inject
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

@MicronautTest
class PostControllerTest() {

    @Inject
    lateinit var posts: PostRepository

    @Inject
    @Client("/")
    lateinit var client: HttpClient

    @Test
    fun `test get posts endpoint`() {
        every { posts.findAll() }
            .returns(
                listOf(
                    Post(
                        id = UUID.randomUUID(),
                        title = "test title",
                        content = "test content",
                        status = Status.DRAFT,
                        createdAt = LocalDateTime.now()
                    )
                )
            )
        val request = HttpRequest.GET<Any>("/posts")
        val bodyType = Argument.listOf(Post::class.java).type
        val response = client.toBlocking().exchange(request, bodyType)

        response.status shouldBe HttpStatus.OK
        response.body()!![0].title shouldBe "test title"

        verify(exactly = 1) { posts.findAll() }
    }

    @MockBean
    fun posts() = mockk<PostRepository>()
}
