# Testing Micronaut applications with JUnit 5

Use JUnit 5 with Micronaut Test to verify beans, persistence, and HTTP
endpoints. This page describes the JUnit examples in this repository and their
database test options.

## Adding JUnit

When generating a Micronaut application, select **JUnit** as the test
framework. The Gradle Micronaut plugin configures the JUnit 5 test runtime with
`testRuntime("junit5")`.

Open *build.gradle* in the project root.

The `micronaut` configuration includes `testRuntime("junit5")`, which adds
JUnit Jupiter to the test runtime.

AssertJ provides fluent assertions, and Mockito provides mock objects. Add
them to the `dependencies` block when your tests need them:

Add the following into the *dependencies* block.

```groovy
testImplementation("org.assertj:assertj-core")
testImplementation("org.mockito:mockito-core")
```

## Test Persistence Layer

Write a test to verify `PostRepository`. Micronaut provides `@MicronautTest`
to start the application context for a test.

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

Here we set `startApplication = false`, it does not start the embedded server to host the application, to test against the database, we do not need a running application

The `data-jdbc` and Jakarta Data examples use the Testcontainers JDBC driver.
Their test configuration points the datasource at a disposable PostgreSQL
container:

```yaml
datasources:
  default:
    url: jdbc:tc:postgresql:18:///postgres
    driverClassName: org.testcontainers.jdbc.ContainerDatabaseDriver
```

The `jdbc:tc:` URL starts the container when the test first connects to the
database. The examples declare the Testcontainers PostgreSQL and JDBC
dependencies in their Gradle build files. Run `./gradlew test` from an example
project directory to run its tests; Docker must be available.

### Manage the container with JUnit

Use the Testcontainers JUnit Jupiter extension when a test needs direct access
to the container or its mapped ports. Annotate the class with `@Testcontainers`
and the static container field with `@Container`. Start the Micronaut context
with the connection properties returned by the container:

```java
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

@Testcontainers
class CustomerDaoTest {
    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:18");

    static ApplicationContext context;

    @BeforeAll
    static void startContext() {
        context = ApplicationContext.run(Map.of(
                "datasources.default.url", postgres.getJdbcUrl(),
                "datasources.default.username", postgres.getUsername(),
                "datasources.default.password", postgres.getPassword(),
                "datasources.default.driverClassName", "org.postgresql.Driver"
        ));
    }
}
```

The Testcontainers extension starts the static container for the test class.
See the [repository test](https://github.com/hantsy/micronaut-sandbox/blob/master/data-jdbc/src/test/java/com/example/CustomerDaoTest.java)
for the complete test. Use JDBC URL mode when you only need a disposable
database; use a JUnit-managed container when the test needs container details
or custom lifecycle control.

## Testing Controller

Write a test for `PostController`.

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

Micronaut's `HttpClient` sends requests to application endpoints. Its default
API is reactive. Call `toBlocking()` when a test needs the blocking API.

The `exchange` method will return a HTTP response object, and the `retrieve` method returns the response body directly.

> Note: When using blocking APIs, if it returns a failure HTTP response, such as return a 4xx status code,  it will throws a `HttpClientResponseException` instead. In contrast,  in ReactiveStreams APIs, it will emit the exception to error channel.

## Integration Tests

The following is an example of integration tests, it tries to test all APIs in an integration environment with a real database, and running on a live embedded server.

```java
@MicronautTest
@Slf4j
class IntegrationTests {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    EmbeddedApplication<?> application;

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }

    @Test
    void testGetAllPosts() {
        var response = client.exchange(HttpRequest.GET("/posts"), String.class);

        var bodyFlux = Flux.from(response).map(HttpResponse::body);
        StepVerifier.create(bodyFlux)
                .consumeNextWith(posts -> assertThat(JsonPath.from(posts).getInt("totalSize")).isGreaterThanOrEqualTo(2))
                .verifyComplete();
    }

    @Test
    public void testCrudFlow() {
        //create a new post
        var request = HttpRequest.POST("/posts", new CreatePostCommand("test title", "test content"));
        var blockingHttpClient = client.toBlocking();
        var response = blockingHttpClient.exchange(request);
        assertThat(response.status().getCode()).isEqualTo(201);
        var savedUrl = response.getHeaders().get("Location");
        assertThat(savedUrl).isNotNull();
        log.debug("saved post url: {}", savedUrl);

        //get by id
        var getPostResponse = blockingHttpClient.exchange(savedUrl, Post.class);
        assertThat(getPostResponse.getStatus().getCode()).isEqualTo(200);

        // add comments
        var addCommentRequest = HttpRequest.POST(savedUrl + "/comments", new CreateCommentCommand("test content"));
        var addCommentResponse = blockingHttpClient.exchange(addCommentRequest);
        assertThat(addCommentResponse.getStatus().getCode()).isEqualTo(201);
        var savedCommentUrl = addCommentResponse.getHeaders().get("Location");
        assertThat(savedCommentUrl).isNotNull();

        // get all comments
        var getAllCommentsRequest = HttpRequest.GET(savedUrl + "/comments");
        var getAllCommentsResponse = blockingHttpClient.exchange(getAllCommentsRequest, Argument.listOf(CommentDetailsDto.class));
        assertThat(getAllCommentsResponse.status().getCode()).isEqualTo(200);
        assertThat(getAllCommentsResponse.body().size()).isEqualTo(1);

        //delete by id
        var deletePostResponse = blockingHttpClient.exchange(HttpRequest.DELETE(savedUrl));
        assertThat(deletePostResponse.getStatus().getCode()).isEqualTo(204);

        //get by id again(404)
        var e = Assertions.assertThrows(HttpClientResponseException.class, () ->
                blockingHttpClient.exchange(HttpRequest.GET(savedUrl)));
        var getPostResponse2 = e.getResponse();
        assertThat(getPostResponse2.getStatus().getCode()).isEqualTo(404);
    }

}
```

In the `testGetAllPosts` test, we try to use reactive `HttpClient` APIs and use reactor-test's `StepVerifier` to assert the data in a reactive data stream.

The second test verifies a flow that creates a post, adds and fetches comments,
and deletes the post.

An API integration test acts as an HTTP client and sends requests to the
application through its public endpoints. The repository's
[`data-jpa` example](https://github.com/hantsy/micronaut-sandbox/tree/master/data-jpa)
also includes tests using RestAssured and the Java HTTP client.

## Use Micronaut Test Resources

The repository includes JUnit 5 projects that use Micronaut Test Resources:
`data-jdbc-test-resources`, `data-jpa-test-resources`, and
`data-mongodb-test-resources`. Their Gradle builds apply the
`io.micronaut.test-resources` plugin. The MongoDB project also declares the
MongoDB Test Resources service and pins its test image in
`src/test/resources/application-test.yml`.

The MongoDB test configuration selects the container image used by the test
resource service:

```yaml
test-resources:
    containers:
        mongodb:
            image-name: mongo:4
```

Test Resources starts the required service for the test run and supplies its
connection settings to the application. This differs from the Testcontainers
JDBC URL shown above, where the test datasource uses the `jdbc:tc:` driver
directly. See the [PostgreSQL Test Resources project](https://github.com/hantsy/micronaut-sandbox/tree/master/data-jdbc-test-resources)
and the [MongoDB Test Resources configuration](https://github.com/hantsy/micronaut-sandbox/tree/master/data-mongodb-test-resources).
