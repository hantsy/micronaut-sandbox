# Testing Micronaut applications

Use the framework you prefer to test Micronaut beans, HTTP endpoints, and
database integrations. These guides focus on the examples in this repository
and show where each framework connects to Micronaut Test.

## Choose a test framework

- [JUnit 5](./junit.md) covers Java tests and the repository's Testcontainers
	and Micronaut Test Resources examples.
- [Kotest](./kotest.md) covers the Kotlin JDBC and coroutine-based R2DBC
	examples, both of which use Testcontainers.
- [Spock](./spock.md) covers Groovy specifications and the MongoDB
	Testcontainers example.

The Test Resources examples in this repository use JUnit 5. The Kotest and
Spock examples use Testcontainers instead.