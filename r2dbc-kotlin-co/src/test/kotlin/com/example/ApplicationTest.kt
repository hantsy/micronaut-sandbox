package com.example

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.inspectors.forAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.reactor.http.client.ReactorHttpClient
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.kotest.annotation.MicronautTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest
@OptIn(ExperimentalCoroutinesApi::class)
class ApplicationTest(
    private val application: EmbeddedApplication<*>,
    @Client("/") private val client: ReactorHttpClient
) : AnnotationSpec() {

    @Test
    fun `test the server is running`() {
        assert(application.isRunning)
    }


    @Test
    fun `test GET all posts endpoint`() {
        runBlocking {
            val response = client.exchange("/posts", Array<Post>::class.java).awaitSingle()
            response.status shouldBe HttpStatus.OK
            response.body()!!.map { it.title }.forAny {
                it shouldContain "Micronaut"
            }
        }
    }

    @Test
    fun `test GET by an none existing id`() {
        runBlocking {
            shouldThrow<HttpClientResponseException> {
                client.exchange("/posts/"+ UUID.randomUUID().toString(), Post::class.java).awaitSingle()
            }.status shouldBe HttpStatus.NOT_FOUND
        }
    }

//    @Test
//    fun `test GET all posts endpoint`() = runBlockingTest {
//        val response = client.exchange("/posts", Array<Post>::class.java).awaitSingle()
//        response.status shouldBe HttpStatus.OK
//        response.body()!!.map { it.title }.forAny {
//            it shouldContain "Micronaut"
//        }
//    }

//    private suspend fun <O> sendRequest(uri: String, type: Class<O>): HttpResponse<O> {
//        return client.exchange(uri, type).awaitSingle()
//    }
}