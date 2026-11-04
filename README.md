# Micronaut Sandbox

Personal playground of experiencing new features of [Micronaut framework](https://micronaut.io).

## Examples

The following table lists all examples in this repository.

| Name       | Language | Description                                          |
|------------|----------|------------------------------------------------------|
| [data-jpa](https://github.com/hantsy/micronaut-sandbox/tree/master/data-jpa)                               | Java     | Data JPA, Testcontainers, JUnit                        |
| [data-jpa-test-resources](https://github.com/hantsy/micronaut-sandbox/tree/master/data-jpa-test-resources) | Java     | Data JPA, Test Resources, JUnit                        |
| [data-jdbc](https://github.com/hantsy/micronaut-sandbox/tree/master/data-jdbc)                               | Java     | Data Jdbc, Testcontainers, JUnit                       |
| [data-jdbc-test-resources](https://github.com/hantsy/micronaut-sandbox/tree/master/data-jdbc-test-resources) | Java     | Data Jdbc, Test Resources, JUnit                       |
| [data-jdbc-jooq](https://github.com/hantsy/micronaut-sandbox/tree/master/data-jdbc-jooq)                   | Java     | Data Jdbc + jOOQ, Testcontainers, JUnit                |
| [data-jdbc-kotlin](https://github.com/hantsy/micronaut-sandbox/tree/master/data-jdbc-kotlin)               | Kotlin   | Data Jdbc, Testcontainers, Kotest                      |
| [data-r2dbc](https://github.com/hantsy/micronaut-sandbox/tree/master/data-r2dbc)                             | Java     | Data R2dbc, Testcontainers, JUnit                      |
| [data-r2dbc-test-resources](https://github.com/hantsy/micronaut-sandbox/tree/master/data-r2dbc-test-resources) | Java   | Data R2dbc, Test Resources, JUnit                      |
| [data-r2dbc-kotlin-co](https://github.com/hantsy/micronaut-sandbox/tree/master/data-r2dbc-kotlin-co)       | Kotlin   | Data R2dbc, Testcontainers, Kotest                     |
| [data-mongo](https://github.com/hantsy/micronaut-sandbox/tree/master/data-mongo)                             | Java     | Data MongoDB, Testcontainers, JUnit                    |
| [data-mongodb-test-resources](https://github.com/hantsy/micronaut-sandbox/tree/master/data-mongodb-test-resources) | Java   | Data MongoDB, Test Resources, JUnit                    |
| [data-hibernate-reactive](https://github.com/hantsy/micronaut-sandbox/tree/master/data-hibernate-reactive) | Java     | Data Hibernate Reactive, Testcontainers, JUnit         |
| [data-mongodb-groovy](https://github.com/hantsy/micronaut-sandbox/tree/master/data-mongodb-groovy)         | Groovy   | Data MongoDB, Testcontainers, Spock                    |
| [mongodb-album-service](https://github.com/hantsy/micronaut-sandbox/tree/master/mongodb-album-service)     | Java     | MongoDB Async, Testcontainers, Spock                   |
| [rxjava3-vertx-pg-client](https://github.com/hantsy/micronaut-sandbox/tree/master/rxjava3-vertx-pg-client) | Java     | RxJava3, Vertx Pg Client, Testcontainers, JUnit        |
| [jakarta-data](https://github.com/hantsy/micronaut-sandbox/tree/master/jakarta-data)                       | Java     | Jakarta Data/Jdbc, Testcontainers, JUnit               |
| [jakarta-data-jpa](https://github.com/hantsy/micronaut-sandbox/tree/master/jakarta-data-jpa)               | Java     | Jakarta Data/Data JPA, Testcontainers, JUnit           |
| [jakarta-data-mongodb](https://github.com/hantsy/micronaut-sandbox/tree/master/jakarta-data-mongodb)       | Java     | Jakarta Data/Data MongoDB, Testcontainers, JUnit       |

## Docs

- Go to [hantsy.github.io/micronaut-sandbox/](https://hantsy.github.io/micronaut-sandbox/).

## Build

### Prerequisites

Make sure you have installed the following software.

- JDK 21
- Gradle 8+
- Docker

### Build & Run

Start up databases.

```bash
docker compose up postgres
```

Enter the project folder.

Run the following command to build the application and run all tests.

```bash
./gradlew build
```

To run the application by Gradle command, use the following command instead.

```bash
./gradlew run
```

## Contribution

Any suggestions are welcome, filing an issue or submitting a PR is also highly recommended.

## References

- [Micronaut Guides](https://docs.micronaut.io/latest/guide/index.html/)
