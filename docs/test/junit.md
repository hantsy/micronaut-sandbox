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

## Test Persistence Layer

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

Here we set `startApplication = false`, it does not start the embedded server to host the application, to test against the database, we do not need a running application

We have add `testcontainers` feature, it will configure a Postgres for test automatically.  Check the testcontainers config in the `src/test/resources/application-test.yml`.

```java
datasources:
  default:
    url: jdbc:tc:postgresql:12:///postgres
    driverClassName: org.testcontainers.jdbc.ContainerDatabaseDriver
```

When there is a `tc` as database host name, testcontainer will start up a Postgres database automatically.

## Testing Controller

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

Similar to Spring's `RestTemplate` or `WebClient`,  Micronaut provides a `HttpClient` to send request to a certain URI, by default it uses the *ReactiveStream*s compatible API, If you are stick on the traditional blocking API, call the `toBlocking()` method to switch to use it. 

The `exchange` method will return a HTTP response object, and the `retrieve` method returns the response body directly.

> Note: When using blocking APIs, if it returns a failure HTTP response, such as return a 4xx status code,  it will throws a `HttpClientResponseException` instead. In contrast,  in ReactiveStreams APIs, it will emit the exception to error channel.

### 

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

The second test mothed is verifying the whole flow of creating a post, add comments, fetching comments, and deleting the post. 

In an API integration tests, test itself works as a Http client(through a HTTP Client library) to interact with the backend with defined APIs. Ideally you can use any HttpClient to test APIs, such as Java 11 HttpClient, OKHttp, etc. There are some examples in the example repository using `RestAssured` and Java 11 new `HttpClient`, check [the source codes](https://github.com/hantsy/micronaut-sandbox/tree/master/post-service) and explore them yourself.

