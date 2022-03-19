package com.example

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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

// mock `deleteAll` and `saveAll` of `PostRepository` which are called in the `DataInitializer` bean.
/*

@MicronautTest()
class PostControllerTest(
   private val server: EmbeddedServer,
) : FunSpec({

    test("test the server is running") {
        assert(server.isRunning)
    }

    test("test get posts endpoint") {
        val postsBean = server.applicationContext.getBean(PostRepository::class.java)
        val client = server.applicationContext.createBean(HttpClient::class.java, server.url)
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
        val response = client.toBlocking().exchange("/posts", Array<Post>::class.java)

        response.status shouldBe HttpStatus.OK
        response.body()!![0].title shouldBe "test title"

        verify(exactly = 1) { posts.findAll() }
    }
}) {
    @MockBean(PostRepository::class)
    fun posts(): PostRepository {
        val mock = mockk<PostRepository>()
        justRun { mock.deleteAll() }
        every { mock.saveAll(any<List<Post>>()) } returns listOf<Post>()
        return mock;
    }
}
*/
//see: https://stackoverflow.com/questions/69644880/how-to-mock-a-bean-in-a-test-written-in-micronaut-kotest
@MicronautTest(environments = ["mock"])
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
        val response = client.toBlocking().exchange("/posts", Array<Post>::class.java)

        response.status shouldBe HttpStatus.OK
        response.body()!![0].title shouldBe "test title"

        verify(exactly = 1) { posts.findAll() }
    }
}) {
    @MockBean(PostRepository::class)
    fun posts() = mockk<PostRepository>()
}
