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
$ ./gradle build
$ java -jar build/demo.jar
```

## Setup Database 

In the root folder of the [sample codes repository](https://github.com/hantsy/micronaut-sandbox/), there is a prepared Docker Compose file, which is used to serve required external service when building and running the sample applications, including Postgres, Mongo, etc.

For example, run the following command to bootstrap a Postgres database instance in Docker.

```bash 
$ docker compose up postgres
```

## Sample Codes

All sample codes of this tutorial are available on Github.

Get a copy of the source codes, and explore them yourself.

```bash 
$ git clone https://github.com/hantsy/micronaut-sandbox
```
