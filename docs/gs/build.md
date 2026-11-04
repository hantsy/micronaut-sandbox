# Building and Running Application

There are several approaches to building and running the application.

## Using IDE 

In an IDE, such as IDEA, it is easy to use the IDE built-in tools to build or rebuild the whole project. Most of modern IDEs include incremental building for code changes automaticially.

To run the application, open the entry class `Application`, click and run it directly like running a  Java application.

## Using Command Line

If it is a Maven project, open terminal and run `mvn clean package` in the project root folder to build the application into a jar archive.

```bash 
$ mvn clean package
```

Then run the build application.

```bash 
$ java -jar target/demo.jar
```

If you prefer Gradle as the build tools, use the following commands instead.

```bash 
$ ./gradlew build
$ java -jar build/libs/demo-0.1-all.jar
```

The `com.gradleup.shadow` plugin writes the fat jar to *build/libs/demo-0.1-all.jar*; the plain `demo-0.1.jar` next to it only contains your own classes.

## Setup Database 

In the root folder of the [sample codes repository](https://github.com/hantsy/micronaut-sandbox/), there is a prepared Docker Compose file, which is used to serve required external service when building and running the sample applications, including Postgres, Mongo, etc.

For example, run the following command to bootstrap a Postgres database instance in Docker.

```bash 
$ docker compose up postgres
```

This is only needed to *run* an application against a stable database. The test suites do not depend on it: each example either uses the Testcontainers JDBC driver / Testcontainers `@Container` (so a disposable container is started and stopped around the test run) or Micronaut Test Resources (which provisions the same containers on demand from the build). Running `./gradlew build` therefore needs a working Docker daemon, but no pre-started database.
