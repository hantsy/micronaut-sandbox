# Testing Micronaut applications with Kotest

Kotest is a Kotlin testing framework that provides assertion matchers and
multiple specification styles. Use it with Micronaut Test to test Kotlin
beans, repositories, and HTTP endpoints. The repository includes JDBC and
coroutine-based R2DBC examples.

## Write a Kotest test

Select Kotest when you generate a project. In a Gradle Kotlin build, the
Micronaut plugin configures the test runtime with `testRuntime("kotest5")`.
Each test class extends a spec style. `StringSpec` names tests with strings,
`FunSpec` declares them with `test(...)` blocks, and `AnnotationSpec` uses
annotations such as `@Test`. Kotest matchers such as `shouldBe` express
assertions. For example, a Micronaut test can use `StringSpec` like this:

```kotlin
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
class ApplicationSpec(
    private val application: EmbeddedApplication<*>
) : StringSpec({
    "the application starts" {
        application.isRunning shouldBe true
    }
})
```

This test uses the `StringSpec` body to register a named test, then checks the
application state with a matcher. Kotest also supports nested contexts and
data-driven test styles; use the style that makes the test cases easiest to
read.

`FunSpec` declares each case in a `test(...)` block:

```kotlin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SumSpec : FunSpec({
    test("adds two numbers") {
        (2 + 3) shouldBe 5
    }
})
```

The [JDBC integration test](https://github.com/hantsy/micronaut-sandbox/blob/master/data-jdbc-kotlin/src/test/kotlin/com/example/IntegrationTests.kt)
starts the embedded server and sends a request with Micronaut's HTTP client.
The repository tests use `StringSpec` for behavior-based test names and
constructor injection for dependencies.

## Mock Micronaut beans

Use Micronaut Test's `@MockBean` to replace a bean in the test application
context. The repository example creates a MockK mock in the `@MockBean` method,
then uses `getMock` to access it for stubbing and verification:

```kotlin
package com.example

import io.kotest.core.spec.style.FunSpec
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension.getMock
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

@MicronautTest(environments = ["mock"])
class PostControllerTest(
    private val postsBean: PostRepository
) : FunSpec({
    test("stubs and verifies the repository") {
        val posts = getMock(postsBean)
        every { posts.findAll() } returns emptyList()

        posts.findAll()
        verify(exactly = 1) { posts.findAll() }
    }
}) {
    @MockBean(PostRepository::class)
    fun posts(): PostRepository = mockk()
}
```

See the [complete controller test](https://github.com/hantsy/micronaut-sandbox/blob/master/data-jdbc-kotlin/src/test/kotlin/com/example/PostControllerFunSpec.kt)
for an example that also sends an HTTP request.

## Run database integration tests

The `data-jdbc-kotlin` and `data-r2dbc-kotlin-co` projects use Testcontainers.
The JDBC project configures the Testcontainers JDBC driver in
`src/test/resources/application-test.yml`:

```yaml
datasources:
  default:
        url: jdbc:tc:postgresql:18:///postgres
        driverClassName: org.testcontainers.jdbc.ContainerDatabaseDriver
```

The coroutine R2DBC project configures both JDBC and R2DBC Testcontainers URLs
in its test configuration. Both projects declare the matching PostgreSQL
Testcontainers dependencies. Run `./gradlew test` from either project; Docker
must be available.

## Test Resources

This repository does not include a Kotest project that uses Micronaut Test
Resources. Its Test Resources examples use JUnit 5; see [JUnit testing](./junit.md)
for those projects. See the [testing overview](./index.md) for the other
framework guides.
