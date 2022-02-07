# Building and Running Application

All sample codes of this tutorial are available on Github.

```bash 
$ git clone https://github.com/hantsy/micronaut-sandbox
```

There are several approaches to building and running the application.

## Using IDE 

If you imported the project into an IDE, such as IDEA, it is easy to use the IDE built-in tools to build or rebuild the project. 

To run the application, open the entry class `Application`, click and run it directly like running a  Java application.

## Using Command Line

If it is a Maven, run `mvn clean package` to build the application into a jar archive.

```bash 
$mvn clean package
```

Then run the generated jar to start up the application.

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

