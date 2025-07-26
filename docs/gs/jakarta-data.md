# Data Access with Micronaut Jakarta Data

Micronaut is a modern, JVM-based framework designed for building cloud-native microservices and Serverless applications. In earlier posts, we explored how to create a RESTful backend application with various data persistence options, including [Data JPA](https://medium.com/itnext/building-restful-apis-with-micronaut-98f4eb39211c), [Data JDBC](https://medium.com/itnext/building-micronaut-applications-with-micronaut-data-jdbc-and-kotlin-81c1b6cf4b10), [Data R2dbc](https://medium.com/itnext/building-micronaut-applications-with-micronaut-data-r2dbc-and-kotlin-coroutines-a1416db5a7d0), [Data MongoDB](https://medium.com/itnext/building-micronaut-applications-with-micronaut-mongo-reative-9c418b403bc1). The Micronaut 4.9 brings Jakarta Data specification(part of Jakarta EE 11) support, as an alternative to the former data persistence solutions. We have discussed [integrating Jakarta Data with Spring](https://medium.com/itnext/integrating-jakarta-data-with-spring-0beb5c215f5f) and [Quarkus](https://medium.com/itnext/integrating-jakarta-data-with-quarkus-0d18365a86fe) in past posts. In this post, we will create a Micronaut application with Jakarta Data. 

## Generating Project Skeleton

Go to the [Micronaut Launch](https://micronaut.io/launch/) page, choose the following options to generate a project skeleton.
* **Micronaut Version**: 4.9.1(the latest stable version at the moment)
* **Java Version**: 21
* **Language**: Java
* **Build Tools**: Gradle Kotlin
* **Test Framework**: JUnit

Keep other options as they are. 

Then, click the **FEATURES** button. In the dialog, add the essential dependencies to your project, including *Jakarta Data*, *Lombok*, *Reactor*, *Data JPA*, *HttpClient*, *Postgres*, and *TestContainers*. Click the **GENERATE** button, and the project skeleton is available as a downloadable archive. Download and extract the files into your local system, and import the project into your favorite IDE, such as IntelliJ IDEA. 

> [!NOTE]
> In the current version, there is a typo error in the generated *build.gradle.kts*, see: https://github.com/micronaut-projects/micronaut-starter/issues/2827. Simply change `implementation("jakarta.data:jakarta-data-api")` to `implementation("jakarta.data:jakarta.data-api:1.0.1")` to overcome this issue temporarily.

I also added **Lombok** to the `testCompileOnly` and `testAnnotationProcessor` scopes, and grouped the dependencies into different categories.  A final modified build script can be found [here](https://github.com/hantsy/micronaut-sandbox/blob/master/jakarta-data-jpa/build.gradle.kts).

## Integrating Jakarta Data 

Before adding Jakarta Data codes, let's create a sample JPA `@Entity` class and an `@Embeddable` class as follows. 

```java
// Customer.java
@Introspected
@Entity
@Table(name = "CUSTOMERS")
@Serdeable
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Customer{
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    UUID id;
    String name;
    Integer age;
    @Embedded  Address address;
    @Version Long version;

    public static Customer of(String name, Integer age, Address address) {
        return new Customer(null, name, age, address, null);
    }
}

// Address.java
@Introspected
@Embeddable
@Serdeable
public record Address(
        String street,
        String city,
        String zip) {

    public static Address of(String street, String city, String zip) {
        return new Address(street, city, zip);
    }
}
```

In the above code fragments, 
* It utilizes Lombok's annotations to generate the `setters`, `getters`, `equals`, and `hashCode`, etc. methods and a constructor with empty arguments that are required by the Jakarta Persistence `Entity` class.
* `@Introspected` is required by the AOT compilation if you want to build a Native build image for your application.
* `@Serdeable` from the Micronaut Serde module, which is a portable Serializable and Deserializable APIs that adopts the existing Jackson, BSON, etc.
* The `@Entity` and `@Table` on the `Customer` class mark it as a Jakarta Persistence `Entity` class and mapped table. The `@Embeddable` annotation on the `Address` class indicates that it is an embeddable component, embedded within the `Customer` entity with a field `address` that is annotated with a `@Embedded` annotation.  The `@Id` annotation indicates that the field is the identifier of the `Customer` entity, and `@GeneratedValue` and `@GenericGenerator` are used to specify the ID generator. The `@Version` field is helpful for optimistic locking within a transaction.

Similar to the `Repository` convention in the Micronaut Data, the Jakarta Data specification also provides a `Repository` abstraction, a top-level `DataRepository` interface, and two `BasicRepository` and `CurdRepository` sub-interfaces. 

Create a `CustomerRepository` as follows.

```java
@Repository
public interface CustomerRepository extends CrudRepository<Customer, UUID> {
}
```

Make sure the `@Repository` and `CrudRepository` are imported from the package `jakarta.data`.

Execute the following command to build the project. 

```bash
./gradlew build
```

After the build progress is complete, expand the' *build/classes*' folder. Besides the `CustomerRepository`, a `CustomerRepository$Intercepted` file will be generated. Open it directly in IDEA, and it will be decompiled into Java source code. 

```java
@Generated
class CustomerRepository$Intercepted implements CustomerRepository, Introduced {
    private final Interceptor[][] $interceptors;
    private final ExecutableMethod[] $proxyMethods;

    public List<Object> updateAll(List<Object> entities) {
        return (List)(new MethodInterceptorChain(this.$interceptors[1], this, this.$proxyMethods[1], new Object[]{entities})).proceed();
    }

    public Object update(Object entity) {
        return (new MethodInterceptorChain(this.$interceptors[2], this, this.$proxyMethods[2], new Object[]{entity})).proceed();
    }

    public List<Object> insertAll(List<Object> entities) {
        return (List)(new MethodInterceptorChain(this.$interceptors[3], this, this.$proxyMethods[3], new Object[]{entities})).proceed();
    }
    // ...
}
```

As you see, all methods defined in the `BasicRepository` and `CrudRepository` are converted into specific methods in the generated class. 

Another two classes, `$CustomerRepository$Intercepted$Definition` and `$CustomerRepository$Intercepted$Definition$Exec`, assist in registering `CustomerRepository` into the Bean context. 

Jakarta Data `Repository` also supports derived queries by methods and custom queries by `@Query`, such as:

```java
@Repository
public interface CustomerRepository extends CrudRepository<Customer, UUID> {
    Optional<Customer> findByName(String name);

    Page<Customer> findByAddressCityLike(String cityLike, PageRequest pageRequest);

    List<Customer> findByAddressZip(String zip, Order<Customer> order);

    @Query("where name like :name")
    @OrderBy("name")
    Customer[] byNameLike(@Param("name") String customerName);
}
```


   





