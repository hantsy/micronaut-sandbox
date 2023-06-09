package com.example

import io.kotest.core.spec.style.StringSpec
import io.kotest.inspectors.forAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
class IntegrationTests(
        private val application: EmbeddedApplication<*>,
        @Client("/") private val client: HttpClient
) : StringSpec({

    "test the server is running" {
        assert(application.isRunning)
    }

    "test GET /posts endpoint" {
        val response = client.toBlocking().exchange("/posts", Array<Post>::class.java)

        response.status shouldBe HttpStatus.OK
        response.body()!!.map { it.title }.forAny {
            it shouldContain "Micronaut"
        }
    }
})
