# Building Restful APIs with Micronaut

Similar to Spring Boot, Micronaut is a JVM based framework and designated for building Microservice and cloud native applications.

Unlike Spring Boot, Micronaut process IOC at compile time and erases runtime reflection, so it is easier to build native image. 

> Spring also started a Spring native project, but it is in the early stage.

For developers that new to Micronaut, it is easy to develop your applications using Micronaut if you have some knowledge of  Spring Boot. In this post, I will share my experience to create a simple Restful API application from scratch using Micronaut from a Spring developer view.

## Generating project skeleton

Similar to [Spring Initializr]( https://start.spring.io), Micronaut provides an online service named **Launch** to help your generate a project skeleton.

Open your browser, go to [Micronaut Launch](https://micronaut.io/launch/), you will see the following screen.

![launch](./launch.png)

In the **Java version** field, select the latest LTS version **17**.  Then click the **Features** button, add *lombok*, *data hibernate jpa*, *assertj*, *postgres*, *testcontainers*. Finally, hit the **GENERATE PROJECT** button to produce the project files into an archive for download.

Extract the project files into disk, and import to your IDE.  

> You can also create a Micronaut project using Micronaut CLI, check [Micronaut Starter documentation](https://micronaut-projects.github.io/micronaut-starter/latest/guide/#installation).

## Exploring project structure

Let's have a look at the files in the project.

```bash
.
├── build.gradle
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
├── micronaut-cli.yml
├── settings.gradle
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── example
    │   │           └── Application.java
    │   └── resources
    │       ├── application.yml
    │       └── logback.xml
    └── test
        ├── java
        │   └── com
        │       └── example
        │           └── DemoTest.java
        └── resources
            ├── application-test.yml
            └── logback-test.xml
```

Besides Gradle build scripts related resources, it is similar to the Spring Boot project structure. 

* The `Application` is the entry class of the application.
* The *src/main/resources/application.yml* is the application configuration.
* The  *src/main/resources/logback.xml*  is logging configuration.
* The `DemoTest` is an example of using `@MicronautTest`. 
* Under the *src/test/resources* folder, there are some config resources for test purpose.

Let's have a look at *build.gradle*.

It uses `com.github.johnrengelman.shadow` to package the application into a jar archive.  The `micronaut` plugin will process the dependency injection at compile time via Java Compiler Annotation Processors. This plugin also includes other tasks, such building application into Docker image and GraalVM native image.

## Declaring a Bean

In Micronaut, it used  JSR330(aka @Inject) specification to annotate the injectable beans. JSR330 originally is lead by SpringSource(now VMware) and Google. 

> Spring also has built-in JSR330 support, by default it is not activated. You should add `inject` in your project dependency.

When a class is annotated with  `@Singleton` means there is only one instance shared in the application scope, `@Prototype` will produce a new instance for every injection.

Micronaut provides a `@Factory` to produces simple beans in groups, for example.

```java
@Factory
class MyConfig{
    
    @Singleton
    public Foo foo(){}
    
    @Singleton
    public Bar bar(){}
}
```

As described in former sections,  Micronaut process IOC at compile time. When building and run the application,  explore the project *build/classes* folder, you will find there are a lot of extra classes generated which names are start with a **$**  symbol.

## Setup database

Open *src/main/resources/application.yml*, the `datasources` is configured when generating the project.  Change the properties according to your environment.

```yaml
datasources:
  default:
    url: jdbc:postgresql://localhost:5432/blogdb
    driverClassName: org.postgresql.Driver
    username: user
    password: password
    schema-generate: CREATE_DROP
    dialect: POSTGRES
jpa.default.properties.hibernate.hbm2ddl.auto: update
```

Create a docker compose file to bootstrap Postgres in docker container.

```yaml
version: '3.7' # specify docker-compose version

services:
  postgres:
    image: postgres
    ports:
      - "5432:5432"
    restart: always
    environment:
      POSTGRES_PASSWORD: password
      POSTGRES_DB: blogdb
      POSTGRES_USER: user
    volumes:
      - ./data:/var/lib/postgresql
      - ./pg-initdb.d:/docker-entrypoint-initdb.d
```

Start up Postgres database.

```bash
docker compose up postgres
```

## Data Accessing with Micronaut Data

We added *data-jpa* feature when generating the project, which enables Micronaut data support. If you have experience of Spring Data JPA , it is easy to migrate to Micronaut Data.

I have used a simple blog application in the former examples when demonstrating other frameworks. In this post, I will reuse the blog application concept.  

Basically it includes two JPA entities, `Post` and `Comment`, it is a `OneToMany` relation.

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "posts")
public class Post implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    UUID id;
    String title;
    String content;

    @Builder.Default
    Status status = Status.DRAFT;

    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true, mappedBy = "post")
    @Builder.Default
    @OrderColumn(name = "comment_idx")
    List<Comment> comments = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return getTitle().equals(post.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle());
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}

// Comment entity 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "comments")
public class Comment implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    private String content;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return getContent().equals(comment.getContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContent());
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

```

They are standard JPA `@Entity` classes.  

An JPA entity class should be annotated with an `@Entity` annotation, and includes a `@Id` field to identify this entity and a none-arguments constructor. Here we use Lombok to generate setters and getters, and constructors at compile time. We use IDE to generate `equals` and `hasCode` according to the business  requirements.  

```java
@Repository
public interface PostRepository extends JpaRepository<Post, UUID>{

}

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByPost(Post post);
}
```

Add a `DataInitializer` bean to initialize some sample data.

```java
@Singleton
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationEventListener<ApplicationStartupEvent> {
    private final PostRepository posts;

    private final TransactionOperations<?> tx;

    @Override
    public void onApplicationEvent(ApplicationStartupEvent event) {
        log.info("initializing sample data...");
        var data = List.of(Post.builder().title("Getting started wit Micronaut").content("test").build(),
                Post.builder().title("Getting started wit Micronaut: part 2").content("test").build());
        tx.executeWrite(status -> {
            this.posts.deleteAll();
            this.posts.saveAll(data);
            return null;
        });
        tx.executeRead(status -> {
            this.posts.findAll().forEach(p -> log.info("saved post: {}", p));
            return null;
        });
        log.info("data initialization is done...");
    }
}
```



Write a test to verify functionality of `PostRepository`. Similar to the `@SpringBootTest`, Micronaut provides a `@MicronautTest`. 

```java
@MicronautTest(application = Application.class, startApplication = false)
class PostRepositoryTest {

    @Inject
    PostRepository posts;

    @PersistenceContext
    EntityManager entityManager;

    @Test
    void testCreatePost() {
        var entity = Post.builder().title("test title").content("test content").build();
        this.entityManager.persist(entity);

        assertThat(entity.getId()).isNotNull();
        assertTrue(posts.findById(entity.getId()).isPresent());
    }

}
```

Here we set `startApplication = false`, it does not start the embedded server to host the application, to test against the database, we do not need a  web environment.

We have add `testcontainers` feature, it will configure a Postgres for test automatically.  Check the testcontainers config in the `src/test/resources/application-test.yml`.

```java
datasources:
  default:
    url: jdbc:tc:postgresql:12:///postgres
    driverClassName: org.testcontainers.jdbc.ContainerDatabaseDriver
```

When there is a `tc` as database host name, testcontainer will start up a Postgres database automaticially.

## Producing Restful APIs

Similar to Spring WebMVC, in Micronaut,  we can use a controller to expose Restful APIs. 

```java
@Controller("/posts")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Validated
public class PostController {
    private final PostRepository posts;
    private final CommentRepository comments;

    @Get(uri = "/", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<List<PostSummaryDto>> getAll() {
        var body = posts.findAll()
                .stream()
                .map(p -> new PostSummaryDto(p.getId(), p.getTitle(), p.getCreatedAt()))
                .toList();
        return ok(body);
    }

    @Get(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<?> getById(@PathVariable UUID id) {
        return posts.findById(id)
                .map(p -> ok(new PostDetailsDto(p.getId(), p.getTitle(), p.getContent(), p.getStatus(), p.getCreatedAt())))
                //.orElseThrow(() -> new PostNotFoundException(id));
        .orElseGet(HttpResponse::notFound);
    }
}
```

A controller is annotated with `@Controller`, you can set a base `uri` that can be applied on all methods.  The `@Get`, `@Post`,`@Put`, `@Delete` is mapped to handle varied HTTP methods, it is similar to the Spring's `@GetMapping`, `@PostMapping`, etc.  You can set media types using *consumes* or *produces*  attributes in these annotations to limit the request and response content type, or use standalone annotations `@Consumes` and `@Produces` to set up the media types.

Start up  the application via Gradle command.

```bash 
./gradlew run
```

> Do not forget  to start up Postgres firstly.

Use `curl` to test the `/posts` endpoint.

```bash
curl http://localhost:8080/posts
[ {
  "id" : "b6fb90ab-2719-498e-a5fd-93d0c7669fdf",
  "title" : "Getting started wit Micronaut",
  "createdAt" : "2021-10-14T22:00:28.80933"
}, {
  "id" : "8c6147ea-8de4-473f-b97d-e211c8e43bac",
  "title" : "Getting started wit Micronaut: part 2",
  "createdAt" : "2021-10-14T22:00:28.80933"
} ]
```


```bash
curl http://localhost:8080/posts/b6fb90ab-2719-498e-a5fd-93d0c7669fdf
 {
  "id" : "b6fb90ab-2719-498e-a5fd-93d0c7669fdf",
  "title" : "Getting started wit Micronaut",
  "content": "test",
  "createdAt" : "2021-10-14T22:00:28.80933"
}
```

> Micronaut CLI provides commands to generate controller, bean, etc.  Run `mn --help` to get all available commands.

Write a test for the `PostController`. 

```java
@MicronautTest(environments = Environment.TEST)
public class PostControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    PostRepository posts;

    @Inject
    CommentRepository comments;

    @MockBean(PostRepository.class)
    PostRepository posts() {
        return mock(PostRepository.class);
    }

    @MockBean(CommentRepository.class)
    CommentRepository comments() {
        return mock(CommentRepository.class);
    }

    @Test
    @DisplayName("test GET '/posts' endpoint")
    public void testGetAllPosts() throws Exception {
        when(this.posts.findAll()).thenReturn(
                List.of(Post.builder().id(UUID.randomUUID()).title("test title").content("test content").build())
        );
        var response = client.toBlocking().exchange("/posts", PostSummaryDto[].class);
        assertEquals(HttpStatus.OK, response.status());
        var body = response.body();
        assertThat(body.length).isEqualTo(1);
        assertThat(body[0].title()).isEqualTo("test title");

        verify(this.posts, times(1)).findAll();
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    @DisplayName("test GET '/posts/{id}' endpoint")
    public void testGetSinglePost() throws Exception {
        when(this.posts.findById(any(UUID.class))).thenReturn(
                Optional.ofNullable(Post.builder().id(UUID.randomUUID()).title("test title").content("test content").build())
        );
        var request = HttpRequest.GET(UriBuilder.of("/posts/{id}").expand(Map.of("id", UUID.randomUUID())));
        var response = client.toBlocking().exchange(request, PostDetailsDto.class);
        assertEquals(HttpStatus.OK, response.status());
        var body = response.body();
        assertThat(body.title()).isEqualTo("test title");

        verify(this.posts, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(this.posts);
    }

    @Test
    @DisplayName("test GET '/posts/{id}' endpoint that does not exist")
    public void testGetSinglePost_notFound() throws Exception {
        when(this.posts.findById(any(UUID.class))).thenReturn(Optional.ofNullable(null));
        var request = HttpRequest.GET(UriBuilder.of("/posts/{id}").expand(Map.of("id", UUID.randomUUID())));
        var exception = assertThrows(HttpClientResponseException.class, () -> client.toBlocking().exchange(request, PostDetailsDto.class));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(this.posts, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(this.posts);
    }
}
```

In this test, we use Mockito to mock all dependent beans(`PostRepository` and `CommentRepository`) in the `PostController`.  To mock beans in the test context, Micronaut provides a `MockBean` to produce a mocked instance to replace the *real* beans.

Similar to Spring's `RestTemplate` or `WebClient`,  Micronaut provides a `HttpClient` to send request to a certain URI, by default it uses the *ReactiveStream*s compatible APIs, If you are stick on the transitional blocking APIs, call the `toBlocking()` method to switch to use the blocking APIs. 

The `exchange` method will return a HTTP response object, and the `retrieve` method returns the response body directly.

Note: If it returns a failure HTTP response, such as return a 4xx status code,  it will throws a `HttpClientResponseException` instead.  



