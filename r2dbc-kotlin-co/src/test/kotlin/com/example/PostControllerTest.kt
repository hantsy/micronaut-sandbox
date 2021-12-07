package com.example

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest.MicronautKotestExtension.getMock
import io.micronaut.test.extensions.kotest.annotation.MicronautTest
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime
import java.util.*

@MicronautTest(environments = ["mock"])
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
                        createdAt = LocalDateTime.now()
                    )
                )
            )
        val response = client.toBlocking().exchange("/posts", Array<Post>::class.java)

        response.status shouldBe HttpStatus.OK
        response.body()!![0].title shouldBe "test title"

        coVerify(exactly = 1) { posts.findAll() }
    }
}) {
    @MockBean(PostRepository::class)
    fun mockedPostRepository() = mockk<PostRepository>()
}
