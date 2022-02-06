# Building Micronaut applications with Micronaut Mongo Reative 

In this post, we will explore the Micronaut Mongo Reactive feature. Unlike the Data JPA and R2dbc, the Mongo Reactive feature is not part of the Micronaut Data project. Micronaut Mongo Reactive is a light-weight wrapper of the official Mongo Java Drivers, and provides autoconfiguration of `MongoClient` from application properties.


## Getting Started

Open your browser and navigate to [Micronaut Launch](https://micronaut.io/launch) to generate a new project skeleton for this post. Select the following items on this page.

*  Java version:  **17**

*  Language: **Java** 

*  Build tool: **Gradle**

*  Test framework: **Spock**

*  Included Features: **mongo-reactive** etc.

Click **GENERATE PROJECT** button to generate a project archive, download it and extract the files into disk, and import to your IDE, such as IDEA.

In the previous examples, we used JUnit and Kotest as testing framework, in this example, we switched to use Spock and Groovy to write tests.

Create a Mongo document entity class.

```kotlin
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class Customer {
    private ObjectId id;
    private String name;
    private int age;
    private Address address;

    public static Customer of(String name, int age, Address address) {
        return Customer.of(null, name, age, address);
    }
}
```

The `Address` is an embedded Document in a  `Customer` Document.

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class Address {
    private String street;
    private String city;
    private String zip;
}
```

Create  a `Repository` class to perform CRUD operations on `Customer`.

```java
@Singleton
@RequiredArgsConstructor
@Slf4j
public class CustomerRepository {
    private final MongoClient mongoClient;
    private final DefaultMongoConfiguration mongoConfiguration;

    public Flux<Customer> findAll() {
        return Flux.from(customersCollection().find());
    }

    public Mono<Customer> findById(ObjectId id) {
        return Mono.from(customersCollection().find(Filters.eq(id)));
    }

    public Mono<ObjectId> insertOne(Customer data) {
        return Mono.from(customersCollection().insertOne(data, new InsertOneOptions().bypassDocumentValidation(false)))
                .mapNotNull(result -> result.getInsertedId().asObjectId().getValue());
    }

    public Mono<Map<Integer, BsonValue>> insertMany(List<Customer> data) {
        return Mono.from(customersCollection().insertMany(data, new InsertManyOptions().bypassDocumentValidation(false).ordered(true)))
                .map(InsertManyResult::getInsertedIds);
    }

    public Mono<Long> deleteById(ObjectId id) {
        return Mono.from(customersCollection().deleteOne(Filters.eq(id), new DeleteOptions()))
                .map(DeleteResult::getDeletedCount);
    }

    public void init() {
        var people = List.of(
                Customer.of("Charles Babbage", 45, Address.of("5 Devonshire Street", "London", "W11")),
                Customer.of("Alan Turing", 28, Address.of("Bletchley Hall", "Bletchley Park", "MK12")),
                Customer.of("Timothy Berners-Lee", 61, Address.of("Colehill", "Wimborne", null))
        );
        Mono.from(customersCollection().drop())
                .then()
                .thenMany(this.insertMany(people))
                .subscribe(
                        result -> result.forEach((key, value) -> log.debug("saved key: {}, value: {}", key, value)),
                        error -> log.debug("initialization failed: {}", error),
                        () -> log.debug("done")
                );
    }

    public Mono<Long> deleteAll() {
        return Mono.from(customersCollection().deleteMany(Filters.empty(), new DeleteOptions()))
                .map(DeleteResult::getDeletedCount);
    }

    private MongoCollection<Customer> customersCollection() {
        return mongoClient
                .getDatabase("userdb")
                .getCollection("customers", Customer.class);
    }

}
```

When a  `mongo.uri` is set in the *application.yml*,  there is a  **reactive** `MongoClient`  bean is available.

In the above codes:

* The `customersCollection()` method defines a Mongo collection mapped to the `Customer` class. As you see, there is a `ObjectId` id field is defined in the `Customer` class, when saving a Customer instance, it will generate a new  ObjectId for it and  saving it to the *customers* document `_id` in MongoDB automatically.
*  The `MongoClient` provides methods for CRUD operations,  but it is based on the *Reactive Streams* APIs. Here we use Reactor API in this project, we use `Mono` and `Flux` to wrap the operation result into Reactor friendly APIs.

Now let's create  a test to test the `CustomerRepository`.

```groovy
@MicronautTest(startApplication = false)
@Slf4j
class CustomerRepositorySpec extends Specification {

    @Inject
    EmbeddedApplication<?> application

    @Inject
    CustomerRepository customerRepository;

    def setup() {
        CountDownLatch latch = new CountDownLatch(1)
        customerRepository.deleteAll()
                .doOnTerminate(_ -> latch.countDown())
                .subscribe(it -> log.debug "deleted customers: {}", it)
        latch.await(1000, TimeUnit.MILLISECONDS)
    }

    void 'application is not running'() {
        expect:
        !application.running
    }

    void 'test findAll'() {
        given:
        this.customerRepository.insertMany(List.of(Customer.of("Jack", 40, null)))
                .block(Duration.ofMillis(5000L))

        when:
        def result = this.customerRepository.findAll()

        then:
        StepVerifier.create(result)
                .expectNextMatches(it -> it.name == "Jack")
                .expectComplete()
                .verify()
    }
}

```

To test the persistence layer, we do not needs a running application. So add `startApplication = false` to the `MicronautTest` annotation. 

Generally, a Spock test is called a  `Specfication`,  you can override the lifecycle methods in your tests,  such as `setup`, `setupSpec` , etc.  Every test follows the BDD rule keywords, such as  `given` , `when` and `then`, etc. 

In the above codes, we override the `setup` method and clear the data in the database. And then create a test to verify the insert and find operations, in the `then` block, we use the `StepVerify` to assert the result in Reactive Streams.

If you want to start up a Testcontainers Docker to serve the required Mongo database,  try to define a  Mongo container instance with `Shared` and `AutoCleanup` annotation, and override `setupSpec` to ensure it is available for all tests in this specification.

```groovy
@Shared
@AutoCleanup
GenericContainer mongo = new GenericContainer("mongo")
    .withExposedPorts(27017)

def setupSpec() {        
    mongo.start()
}
```

Like the previous examples,  we can listen a `ServerStartupEvent` to initialize the sample data.

```java
@Singleton
@Requires(notEnv = "mock")
@Slf4j
@RequiredArgsConstructor
public class DataInitializer {
    private final CustomerRepository customerRepository;

    @EventListener
    public void onStart(ServerStartupEvent event) {
        log.debug("starting data initialization...");
        this.customerRepository.init();
    }
}
```

Try to create a  controller to expose the RESTful APIs.

```java
@Controller("/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {
    private final CustomerRepository customerRepository;

    @Get(uri = "/", produces = {MediaType.APPLICATION_JSON})
    public Flux<?> all() {
        return this.customerRepository.findAll();
    }

    @Get(uri = "/{id}", produces = {MediaType.APPLICATION_JSON})
    public Mono<MutableHttpResponse<Customer>> byId(@PathVariable ObjectId id) {
        return this.customerRepository.findById(id)
                .map(HttpResponse::ok)
                .switchIfEmpty(Mono.just(notFound()));
    }

    @Post(uri = "/", consumes = {MediaType.APPLICATION_JSON})
    public Mono<HttpResponse<?>> create(@Body Customer data) {
        return this.customerRepository.insertOne(data)
                .map(id -> created(URI.create("/customers/" + id.toHexString())));
    }

    @Delete(uri = "/{id}")
    public Mono<HttpResponse<?>> delete(@PathVariable ObjectId id) {
        return this.customerRepository.deleteById(id)
                .map(deleted -> {
                    if (deleted > 0) {
                        return noContent();
                    } else {
                        return notFound();
                    }
                });
    }
}

```

To process the `ObjectId` in the request path, create a `TypeConverter` to convert  id from String to `ObjectId`.

```java
@Singleton
public class StringToObjectIdConverter implements TypeConverter<String, ObjectId> {

    @Override
    public Optional<ObjectId> convert(String object, Class<ObjectId> targetType, ConversionContext context) {
        return Optional.of(new ObjectId(object));
    }
}
```

 In order to serialize the id (`ObjectId` type) of `Customer` as a String in the HTTP response, create a `JsonSerializer` to customize the serialization process.  When it is applied,  the id field is serialized as a hex string instead of a JSON object.

```java
@Singleton
public class ObjectIdJsonSerializer extends JsonSerializer<ObjectId> {
    
    @Override
    public void serialize(ObjectId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.toHexString());
    }
}
```

Create a test for the `CustomerController`.

```gro
@MicronautTest(environments = ["mock"])
class CustomerControllerSpec extends Specification {

    @Inject
    EmbeddedApplication<?> application

    @Inject
    @Client("/")
    ReactorHttpClient client

    @Inject
    CustomerRepository customerRepository

    def 'test it works'() {
        expect:
        application.running
    }

    void 'get all customers'() {
        given:
        1 * customerRepository.findAll() >> Flux.just(Customer.of(ObjectId.get(), "Jack", 40, null), Customer.of(ObjectId.get(), "Rose", 20, null))

        when:
        Flux<HttpResponse<String>> resFlux = client.exchange(HttpRequest.GET("/customers"), String).log()

        then:
        //1 * customers.findAll() >> Flux.just(Customer.of(ObjectId.get(), "Jack", 40, null), Customer.of(ObjectId.get(), "Rose", 20, null))
        StepVerifier.create(resFlux)
        //.expectNextCount(1)
                .consumeNextWith(s -> {
                    assert s.getStatus() == HttpStatus.OK
                    assert s.body().contains('Jack')
                })
                .expectComplete()
                .verify()
    }

    void 'create a new customer'() {
        given:
        def objId = ObjectId.get()
        1 * customerRepository.insertOne(_) >> Mono.just(objId)

        when:
        def body = Customer.of(null, "Jack", 40, null)
        Flux<HttpResponse<String>> resFlux = client.exchange(HttpRequest.POST("/customers", body), String).log()

        then:
        StepVerifier.create(resFlux)
                .consumeNextWith(s -> {
                    assert s.getStatus() == HttpStatus.CREATED
                    assert s.header("Location") == '/customers/' + objId.toHexString()
                })
                .expectComplete()
                .verify()
    }

    void 'get customer by id '() {
        given:
        1 * customerRepository.findById(_) >> Mono.just(Customer.of(ObjectId.get(), "Jack", 40, null))

        when:
        Flux<HttpResponse<String>> resFlux = client.exchange(HttpRequest.GET("/customers/" + ObjectId.get().toHexString()), String).log()

        then:
        StepVerifier.create(resFlux)
                .consumeNextWith(s -> {
                    assert s.getStatus() == HttpStatus.OK
                    assert s.body().contains('Jack')
                })
                .expectComplete()
                .verify()
    }

    void 'get customer by none-existing id '() {
        given:
        1 * customerRepository.findById(_) >> Mono.empty()

        when:
        Flux<HttpResponse<String>> resFlux = client.exchange(HttpRequest.GET("/customers/" + ObjectId.get().toHexString()), String).log()

        then:
        StepVerifier.create(resFlux)
                .consumeErrorWith(error -> {
                    assert error instanceof HttpClientResponseException
                    assert (error as HttpClientResponseException).status == HttpStatus.NOT_FOUND
                })
                .verify()
    }

    void 'delete customer by id '() {
        given:
        1 * customerRepository.deleteById(_) >> Mono.just(1L)

        when:
        Flux<HttpResponse<String>> resFlux = client.exchange(HttpRequest.DELETE("/customers/" + ObjectId.get().toHexString()), String).log()

        then:
        StepVerifier.create(resFlux)
                .consumeNextWith(s -> {
                    assert s.getStatus() == HttpStatus.NO_CONTENT
                })
                .expectComplete()
                .verify()
    }

    void 'delete customer by none-existing id '() {
        given:
        1 * customerRepository.deleteById(_) >> Mono.just(0L)

        when:
        Flux<HttpResponse<String>> resFlux = client.exchange(HttpRequest.DELETE("/customers/" + ObjectId.get().toHexString()), String).log()

        then:
        StepVerifier.create(resFlux)
                .consumeErrorWith(error -> {
                    assert error instanceof HttpClientResponseException
                    assert (error as HttpClientResponseException).status == HttpStatus.NOT_FOUND
                })
                .verify()
    }

    @MockBean(CustomerRepository)
    CustomerRepository mockedCustomerRepository() {// must use explicit type declaration
        Mock(CustomerRepository)
    }
}
```

In this test, we create a mock bean for `CustomerRepository`, note you have to declare type explicitly. In the `given` block, it setup the assumptions  and assertations in a single place. 

Another great feature of Mongo is  the Gridfs support. For those home-use cloud applications it is a simple alternative of AWS S3 storage service.

Next we will create a simple upload and download endpoint to store binary data into Mongo Gridfs storage and retrieve it from the Gridfs storage.

Firstly declare a  `GridFSBucket` bean.

```java
@Factory
public class GridFSConfig {

    @Bean
    GridFSBucket gridFSBucket(MongoClient client) {
        return GridFSBuckets.create(client.getDatabase("photos"))
                .withChunkSizeBytes(4096)
                //.withReadConcern(ReadConcern.MAJORITY)
                .withWriteConcern(WriteConcern.MAJORITY);
    }
}
```

Now create a controller to handle the file upload and download.

```java
@Controller("/photos")
@RequiredArgsConstructor
@Slf4j
public class PhotoController {

    private final GridFSBucket bucket;

    @Post(uri = "/", consumes = {MediaType.MULTIPART_FORM_DATA})
    public Mono<HttpResponse<?>> upload(StreamingFileUpload file) {
        var filename = file.getFilename();
        var name = file.getName();
        var contentType = file.getContentType();
        var size = file.getSize();
        log.debug("uploading file...\n filename:{},\n name:{},\n contentType: {},\n size: {} ", filename, name, contentType, size);
        var options = new GridFSUploadOptions();
        contentType.ifPresent(c -> options.metadata(new Document("contentType", c)));
        return Mono.from(this.bucket.uploadFromPublisher(
                                filename,
                                Mono.from(file).mapNotNull(partData -> {
                                    try {
                                        return partData.getByteBuffer();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return null;
                                }),
                                options
                        )
                )
                .map(ObjectId::toHexString)
                .map(id -> ok(Map.of("id", id)));
    }

    @Get(uri = "/{id}", produces = {MediaType.APPLICATION_OCTET_STREAM})
    public Mono<HttpResponse<?>> download(@PathVariable ObjectId id) {
        return Mono.from(this.bucket.downloadToPublisher(id))
                .map(HttpResponse::ok);
    }

    @Delete(uri = "/{id}")
    public Mono<HttpResponse<?>> delete(@PathVariable ObjectId id) {
        return Mono.from(this.bucket.delete(id))
                .map(v -> noContent());
    }
}

```

To upload a file, use `bucket.uploadFromPublisher` to transfer the upload data into a Gridfs bucket. To download a file, call `downloadToPublisher` to read data info a `ByteBuffer`. To remove it, just invoke the delete method.

Get the complete [source codes](https://github.com/hantsy/micronaut-sandbox/tree/master/album-service) from my Github.









