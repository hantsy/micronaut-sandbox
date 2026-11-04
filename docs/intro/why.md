# Why Micronaut?

Micronaut provides compile-time dependency injection, application APIs for
HTTP and data access, and integrations with multiple JVM languages and test
frameworks. The examples in this guide demonstrate these capabilities:

- **Compile-time dependency injection:** Micronaut processes bean definitions during compilation instead of relying on runtime classpath scanning.
- **Data access:** Micronaut Data supports JDBC, JPA, R2DBC, and MongoDB integrations. The examples include repository APIs and query specifications.
- **Testing options:** The repository includes JUnit 5, Kotest, and Spock examples, with Testcontainers and Micronaut Test Resources examples for database integration tests.
- **Application lifecycle:** Micronaut provides lifecycle events for initializing application components.
- **Ahead-of-time compilation:** Micronaut's build plugins support ahead-of-time processing and native-image builds. See the [Micronaut documentation](https://docs.micronaut.io/) for configuration details.