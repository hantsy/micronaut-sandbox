# Why Micronaut?

Micronaut developers adopts the good parts from the existing Java frameworks, such as Grails, Spring Boot,Quarkus. So even Micronaut is relatedly young, but it includes powerful and concise APIs for developers.

Here we list some highlight features, 

* The Micronaut Data and controller APIs are similar to Spring Data and Spring WebMvc. For relational database support, Micronaut Data provides more powerful and united annotations to both Jdbc and R2dbc, and it brings the programmatic JPA `Specification` to Jdbc/R2dbc queries. 
* Great parts of the existing Grails framework, esp. Gorm is available in Micronaut. 
* More testing frameworks and tools support. Besides JUnit, it supports KoTest and Spock officially.
* More concise application lifecycle event abstract, make it suitable for different application types, from serverless application to general web application.
* Through Maven plugin or Gradle plugin provides AOT compiling support. The annotations of Dependency Injection, Bean Validation and Jakarta Transaction are processed at compile time instead of dynamic invocation by Java reflection APIs at runtime, which makes it possible to build Micronaut applications into GraalVM native executable binaries. And Redhat [Quarkus](https://www.quarkus.io) uses a similar approach to handle CDI beans at compile time. Micronaut and Quarkus affect the next generation CDI specification. There is a new sub specification under CDI named CDI Lite(maybe the naming is not good), which tries to define a united behavior of processing CDI beans at compile time. Check the [Eclipse implementation of CDI Lite](https://projects.eclipse.org/projects/ee4j.odi).