# Testing Micronaut Application with JUnit

JUnit is the most popular testing framework for Java developers. 

## Adding JUnit 

When generating a Micronaut application, make sure you have selected **JUnit** testing framework.

Open *build.gradle* in the project root.

In the *micronaut* task configuration, there is *testRuntime("junit5")*, it sets up the *junit-jupiter* in test scope.

*AssertJ* and *Mockito* are usually used with JUnit. The former provides fluent asserting APIs, and the later is the defact mocking framework.

Add the following into the *dependencies* block.

```groovy 
testImplementation("org.assertj:assertj-core")
testImplementation("org.mockito:mockito-core")
```

