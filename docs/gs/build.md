# Build and run an application

Each example is a standalone Gradle project. Run its wrapper from the project
directory to build or test it.

## Use an IDE

Import the project into your IDE and use its Gradle integration to run the
`build`, `test`, or `run` task. You can also run the `Application` class from
the IDE.

## Use the command line

From the example project directory, build and test with the Gradle wrapper:

```bash
./gradlew build
```

On Windows, run `gradlew.bat build` instead. To run the application with
Gradle, use:

```bash
./gradlew run
```

The Shadow plugin also creates a runnable JAR. Its path includes the project
name and version; inspect `build/libs/` for the generated file.

## Start a database

The repository root contains a [Docker Compose file](https://github.com/hantsy/micronaut-sandbox/blob/master/docker-compose.yml)
for services used by the examples. To run a sample against the local
PostgreSQL service, start it from the repository root:

```bash
docker compose up postgres
```

Tests do not need a pre-started database. Examples that use Testcontainers or
Micronaut Test Resources start a disposable service for the test run, so Docker
must be available when you run `./gradlew test` or `./gradlew build`.
