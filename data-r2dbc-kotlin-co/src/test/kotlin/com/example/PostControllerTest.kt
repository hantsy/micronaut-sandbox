package com.example

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest.MicronautKotestExtension.getMock
import io.micronaut.test.extensions.kotest.annotation.MicronautTest
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.*


@MicronautTest(environments = ["mock"], transactional=false)
class PostControllerTest(
    private val postRepository: PostRepository,
    @Client("/") private var client: HttpClient
) : FunSpec({

    test("test get posts endpoint") {
        val posts = getMock(postRepository)
        coEvery { posts.findAll() }
            .returns(
                flowOf(
                    Post(
                        id = UUID.randomUUID(),
                        title = "test title",
                        content = "test content",
                        status = Status.DRAFT,
                        createdAt = LocalDateTime.now(),
                        comments = emptyList()
                    )
                )
            )
        val response = client.toBlocking().exchange("/posts", Array<Post>::class.java)

        response.status shouldBe HttpStatus.OK
        response.body()!![0].title shouldBe "test title"

        coVerify(exactly = 1) { posts.findAll() }
    }

    test("create post with invalid input data") {
        val posts = getMock(postRepository)
        coEvery { posts.save(any<Post>()) }
            .returns(
                Post(
                    id = UUID.randomUUID(),
                    title = "test title",
                    content = "test content",
                    status = Status.DRAFT,
                    createdAt = LocalDateTime.now(),
                    comments = emptyList()
                )
            )
        val request = HttpRequest.POST("/posts", CreatePostCommand(title = "", content = ""))
        val exception: HttpClientResponseException = assertThrows {
            val response: HttpResponse<Any> = client.toBlocking().exchange(request)
        }

        exception.status shouldBe HttpStatus.BAD_REQUEST
        coVerify(exactly = 0) { posts.findAll() }
    }
}) {
    @MockBean(PostRepository::class)
    fun mockedPostRepository() = mockk<PostRepository>()
}
