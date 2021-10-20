package com.example

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest.MicronautKotestExtension.getMock
import io.micronaut.test.extensions.kotest.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import java.util.*

@MicronautTest
class PostControllerTest(
    private val postsBean: PostRepository,
    @Client("/") private var client: HttpClient
) : FunSpec({

    test("test get posts endpoint") {
        val posts = getMock(postsBean)
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
}) {
    @MockBean(PostRepository::class)
    fun posts() = mockk<PostRepository>()
}
