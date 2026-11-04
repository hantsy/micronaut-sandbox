# Testing Micronaut applications with Spock

Spock is a testing and specification framework for Java and Groovy. It uses
Groovy's expressive syntax, a `Specification` base class, and labeled blocks
to describe test behavior. The `data-mongodb-groovy` example combines Spock,
Micronaut Test, and Testcontainers to test a MongoDB repository and HTTP
controller.

## Write a Spock specification

Create a class that extends `spock.lang.Specification`, then add feature methods
whose names describe the behavior. Spock organizes a test around setup,
stimulus, and assertions. Its `given:`, `when:`, and `then:` blocks make those
phases explicit:

```groovy
import spock.lang.Specification

class SumSpec extends Specification {

    def "adds two numbers"() {
        given: "two numbers"
        def left = 2
        def right = 3

        when: "I add them"
        def result = left + right

        then: "the sum is returned"
        result == 5
    }
}
```

Use `expect:` for a compact setup-and-assertion test, `where:` for data-driven
cases, and `setup()` or `cleanup()` for per-feature lifecycle work. The
repository's
[`CustomerControllerSpec`](https://github.com/hantsy/micronaut-sandbox/blob/master/data-mongodb-groovy/src/test/groovy/com/example/CustomerControllerSpec.groovy)
uses `@MicronautTest` to start the application and inject the HTTP client and
repository.

The project selects Spock through the Micronaut Gradle configuration:

```groovy
micronaut {
    testRuntime("spock2")
}
```

## Mock Micronaut beans

Use a Micronaut Test `@MockBean` method to replace a bean in the test
application context. Return a Spock `Mock` from the method, then stub and verify
interactions in a feature method:

```groovy
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class CustomerControllerSpec extends Specification {
    @Inject
    CustomerRepository customerRepository

    def "returns customers from the repository"() {
        when:
        def customers = customerRepository.findAll()

        then:
        1 * customerRepository.findAll() >> []
        customers.empty
    }

    @MockBean(CustomerRepository)
    CustomerRepository mockCustomerRepository() {
        Mock(CustomerRepository)
    }
}
```

In the `then:` block, `1 *` requires one call and `>>` provides the stubbed
return value. See the repository's
[`CustomerControllerSpec`](https://github.com/hantsy/micronaut-sandbox/blob/master/data-mongodb-groovy/src/test/groovy/com/example/CustomerControllerSpec.groovy)
for examples that stub repository methods while exercising HTTP endpoints.

## Run MongoDB integration tests

The repository's
[`CustomerRepositorySpec`](https://github.com/hantsy/micronaut-sandbox/blob/master/data-mongodb-groovy/src/test/groovy/com/example/CustomerRepositorySpec.groovy)
uses a shared `MongoDBContainer`. It passes the container's mapped port to a
Micronaut `ApplicationContext`, so the repository test uses a real MongoDB
instance without starting the HTTP server.

The Gradle project declares the Spock integration and MongoDB container
dependencies:

```groovy
testImplementation("org.testcontainers:testcontainers-mongodb")
testImplementation("org.testcontainers:testcontainers-spock")
testImplementation("org.testcontainers:testcontainers")
```

Run `./gradlew test` from `data-mongodb-groovy`; Docker must be available. The
repository does not currently include a Spock project using Micronaut Test
Resources. Its Test Resources examples use JUnit 5; see [JUnit testing](./junit.md).