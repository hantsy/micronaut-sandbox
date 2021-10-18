# micronaut-sandbox

A  personal sandbox project  to experience the new features of the [Micronaut  framework](https://micronaut.io).

## Docs

* [Building RESTful APIs with Micronaut](./docs/gs-rest.md)

## Sample Codes

| Name | Description |
|:-------------------|:-------------------------------------------------|
|[post-service](https://github.com/hantsy/micronaut-sandbox/tree/master/post-service) |Simple CURD RESTful APIs|

## Build

### Prerequisites

Make sure you have installed the following software.

* JDK 17 
* Gradle 7.2+ or Apache Maven 3.8.x(if you prefer Maven as build tools)
* Docker

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

* [Micronaut Guides](https://docs.micronaut.io/latest/guide/index.html/)

