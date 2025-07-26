# Data Access with Micronaut Jakarta Data

Micronaut is a modern, JVM-based framework designed for building cloud-native microservices and serverless applications. Previously, we explored how to create RESTful backend applications with various Micronaut Data modules, including [Data JPA](https://medium.com/itnext/building-restful-apis-with-micronaut-98f4eb39211c), [Data JDBC](https://medium.com/itnext/building-micronaut-applications-with-micronaut-data-jdbc-and-kotlin-81c1b6cf4b10), [Data R2dbc](https://medium.com/itnext/building-micronaut-applications-with-micronaut-data-r2dbc-and-kotlin-coroutines-a1416db5a7d0), and [Data MongoDB](https://medium.com/itnext/building-micronaut-applications-with-micronaut-mongo-reative-9c418b403bc1).

With the release of Micronaut 4.9, support for the Jakarta Data specification (introduced in Jakarta EE 11) is now available, providing a *standardized* alternative to traditional data persistence approaches. In previous articles, we explored how to [integrate Jakarta Data with Spring](https://medium.com/itnext/integrating-jakarta-data-with-spring-0beb5c215f5f) and [Quarkus](https://medium.com/itnext/integrating-jakarta-data-with-quarkus-0d18365a86fe). In this guide, we'll focus on using Jakarta Data within a Micronaut application to handle data access.

## Generating the Project Skeleton

Navigate to the [Micronaut Launch](https://micronaut.io/launch/) page and select the following options to generate your project skeleton:

* **Micronaut Version**: 4.9.1 (latest stable version at the time of writing)
* **Java Version**: 21
* **Language**: Java
* **Build Tools**: Gradle Kotlin
* **Test Framework**: JUnit

Keep the other options at their defaults.

Next, click the **FEATURES** button and add these essential dependencies: *Jakarta Data*, *Lombok*, *Reactor*, *Data JPA*, *HttpClient*, *Postgres*, and *TestContainers* in the dialog.

Then click **GENERATE** button to download the generated archive, extract the files to your local system, and import the project into your favorite IDE, such as IntelliJ IDEA.

> [!NOTE]
> There is a typo in the generated *build.gradle.kts* in the current version. For more details, see: https://github.com/micronaut-projects/micronaut-starter/issues/2827. Simply change `implementation("jakarta.data:jakarta-data-api")` to `implementation("jakarta.data:jakarta.data-api:1.0.1")` to resolve this issue temporarily.

Additionally, add **Lombok** to the `testCompileOnly` and `testAnnotationProcessor` scopes, and organize the dependencies for clarity. You can check the final modified build script [here](https://github.com/hantsy/micronaut-sandbox/blob/master/jakarta-data-jpa/build.gradle.kts).

## Integrating Jakarta Data

The Jakarta Data specification does not prescribe how entities should be defined. In Micronaut the entity definitions rely on the conventions and approaches provided by Micronaut Data.

Let's start by creating a simple Jakarta Persistence `@Entity` class and an `@Embeddable` class:

```java
// Customer.java
@Introspected
@Entity
@Table(name = "CUSTOMERS")
@Serdeable
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Customer {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    UUID id;
    String name;
    Integer age;
    @Embedded Address address;
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

In the above code fragments:

* Lombok annotations automatically generate setters, getters, `equals`, `hashCode`, and a no-argument constructor, all of which are required for Jakarta Persistence `Entity` classes.
* `@Introspected` is necessary for Ahead-of-Time (AOT) compilation, especially if you plan to build a native image of your application.
* `@Serdeable` (from the Micronaut Serde module) provides portable serialization and deserialization support compatible with formats like Jackson and BSON.
* The `@Entity` and `@Table` annotations on the `Customer` class designate it as a Jakarta Persistence entity and specify the corresponding database table. The `@Embeddable` annotation on the `Address` class marks it as a component that can be embedded within other entities, such as the `address` field in `Customer`, which is annotated with `@Embedded`. The `@Id` annotation identifies the primary key field, while `@GeneratedValue` and `@GenericGenerator` configure the ID generation strategy. The `@Version` field enables optimistic locking for transactional consistency.

Just like Micronaut Data, Jakarta Data introduces a `Repository` abstraction to simplify data access. At its core is the top-level `DataRepository` interface, which is extended by `BasicRepository` and `CrudRepository` for common CRUD operations.

You can define a `CustomerRepository` as shown below:

```java
@Repository
public interface CustomerRepository extends CrudRepository<Customer, UUID> {
}
```

Ensure that both `@Repository` and `CrudRepository` are imported from the `jakarta.data` package.

To build the project, run:

```bash
./gradlew build
```

Once the build completes, navigate to the *build/classes* directory. In addition to the `CustomerRepository` class, you will find a generated `CustomerRepository$Intercepted` file. You can open this file in your IDE (such as IntelliJ IDEA), where it will be decompiled into readable Java source code.

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

As you can see, all methods defined in `BasicRepository` and `CrudRepository` are translated into concrete implementations within the generated class.

Additionally, two helper classes-`$CustomerRepository$Intercepted$Definition` and `$CustomerRepository$Intercepted$Definition$Exec` are generated to facilitate the registration of `CustomerRepository` into the Micronaut Bean context.

The Jakarta Data `Repository` abstraction also supports derived queries by method names, pagination, and custom queries using the `@Query` annotation. For example:

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

All of these methods closely resemble those found in the existing `Repository` abstractions from Micronaut Data or Spring Data, making them intuitive and straightforward to use.

> [!NOTE]
> Jakarta Data uses 1-based pagination, so page numbers start at 1 instead of 0.

```java
customerRepository.findByAddressCityLike("New%", PageRequest.of(1, 10, true));
```

Another compelling aspect of Jakarta Data is its support for lifecycle-based methods that automatically infer the entity type from method parameters or return types. This allows you to define flexible, free-form interfaces for performing simple CRUD operations on your entities, without being tied to a specific repository abstraction.

```java
@Repository
public interface CustomerDao {
    @Find
    @OrderBy("name")
    List<Customer> findAll();

    @Find
    Optional<Customer> findById(@By(ID) UUID id);

    // @Find
    // List<Customer> findByCity(@By("address.city") String city, Limit limit, Sort<?>... sort);

    @Insert
    Customer save(Customer data);

    @Update
    void update(Customer data);

    @Delete
    void delete(Customer data);
}
```

Please note that certain methods available in Hibernate Data implementations are currently not supported in this context. For more details, refer to [micronaut-data#3487](https://github.com/micronaut-projects/micronaut-data/issues/3487). Additionally, invoking the underlying data store handler within custom `default` methods is not yet possible, as discussed in [micronaut-data#3490](https://github.com/micronaut-projects/micronaut-data/issues/3490).

You can explore the [complete example project on GitHub](https://github.com/hantsy/micronaut-sandbox/tree/master/jakarta-data-jpa), which also demonstrates testing against a real database using Testcontainers.

## JDBC Support

Unlike the Jakarta Data implementation Hiberante, which is heavily dependent on Hibernate's `StatelessSession`. Micronaut Data extends Jakarta Data support to all its data modules, including JDBC and MongoDB.

To use Jakarta Data with JDBC, simply select `Data JDBC` instead of `Data JPA` when generating your project skeleton.

In your `CustomerRepository` interface, annotate it with both `@Repository` from Jakarta Data and `@JdbcRepository` from Micronaut Data:

```java
@Repository
@JdbcRepository
public interface CustomerRepository extends CrudRepository<Customer, UUID> {
    // ...
}
```

You can define the `Customer` entity using the standard Micronaut Data annotations:

```java
// Customer.java
@Introspected
@MappedEntity(value = "customers")
@Serdeable
public record Customer(
        @Id @AutoPopulated UUID id,
        String name,
        Integer age,
        @Relation(EMBEDDED) Address address,
        @Version Long version
) {
    public static Customer of(String name, Integer age, Address address) {
        return new Customer(null, name, age, address, null);
    }
}

// Address.java
@Introspected
@Embeddable
@Serdeable
public record Address(
        @MappedProperty("street") String street,
        @MappedProperty("city") String city,
        @MappedProperty("zip") String zip
) {
    public static Address of(String street, String city, String zip) {
        return new Address(street, city, zip);
    }
}
```

Micronaut Data Jdbc allows you to define entities with Jakarta Persistence API. If you prefer to use Jakarta Persistence annotations, add the `jakarta.persistence-api` dependency to your project.

You can find the [complete example project](https://github.com/hantsy/micronaut-sandbox/tree/master/jakarta-data) updated for JDBC.

## MongoDB Support

Similarly, to use Jakarta Data with MongoDB, select `Data MongoDB` instead of `Data JPA` when generating your project, and remove `Postgres` from the feature list.

Micronaut Data MongoDB also reuses the same data annotations to manage entities. However, by default, it does not support `UUID` as an ID type; use a `String` or MongoDB-specific `ObjectId` instead.

```java
// Customer.java
@Introspected
@MappedEntity(value = "customers")
@Serdeable
public record Customer(
        @Id @AutoPopulated String id,
        // ...
) {
    // ...
}
```

Annotate your `CustomerRepository` interface with both `@Repository` and `@MongoRepository`:

```java
@Repository
@MongoRepository
public interface CustomerRepository extends CrudRepository<Customer, String> {
    // ...
}
```

You can explore the [complete example project](https://github.com/hantsy/micronaut-sandbox/tree/master/jakarta-data-mongo) updated for MongoDB.

## Summary

With Micronaut 4.9, support for the Jakarta Data specification introduces a *standardized* way to handle data access, providing an alternative approach for working with both relational databases and NoSQL stores.
