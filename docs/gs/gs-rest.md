# Building RESTful API with Micronaut Data JPA

In this section, we are building a RESTful API backend application with Micronaut Data JPA. If you have some experience with Spring Boot and Spring Data JPA, it is easy to update yourself to use Micronaut to archive the same purpose.

## Preparing Project Skeleton

Follow [Generating Project Skeleton](./gen) guide and generate a new project skeleton.

### Generating Project

Open your browser and navigate to [Micronaut Launch](https://micronaut.io/launch), fill the following fields in the [Micronaut Launch](https://micronaut.io/launch) page, leave other as it is.

* Java version:  **17**
* Language: **Java** 
* Build tool: **Gradle**
* Test framework: **Junit**
* Included Features: **lombok**, **data hibernate jpa**, **assertj**, **postgres**, **testcontainers** etc.

Import the generated project into your IDE.


### Configuring Database

In this project we are using Postgres as database. You can download a copy of Postgres and install it in your local system, then  create a new database to serve the application.

Open *src/main/resources/application.yml*, there is a  *default* datasource  is configured by default.

Change the properties according to your environment.

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

Alternatively you can serve a Postgres datasse in Docker quickly. 

### Serving  Postgres Database in Docker

Create a *docker-compose.yml* file, and define a *postgres* service as the following.

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

Then start the Postgres database instance in the Docker container.

```bash
docker compose up postgres
```
Wait for a while, it will prepare a Postgres database from Docker image and start it.

## Data Accessing using Micronaut Data JPA

When generating the project, we have add a **data-jpa** feature into the project dependencies, which enables Micronaut Data JPA support. 

Similar to Spring Data architecture,  Micronaut Data also provides a common abstraction for the basic data operations, For example, there is a `GenericRepository` interface to indicate it is a  `Repository` for JPA entities,  and its sub interfaces, such as `CrudRepository` and `PagableRepsoitory` includes more operations, such as *save*,  *retrieve*, *update*, *delete*, and bulk updates, and return *pageable* results for large amount of results. 

Micronaut Data JPA has similar APIs with Spring Data JPA, it also contains a pragmatic criteria builder to execute query via custom `Specificaiton`.

Currently  Micronaut Data project only supports relational database, it includes 3 modules: Data JPA, Data JDBC, Data R2DBC,  read [the official documentation](https://micronaut-projects.github.io/micronaut-data/latest/guide/) for more details.

In this post, we focus on the Micronaut Data JPA.

Next, we will create a JPA entity and create a Repository for the entity, then create a  Controller to produce RESTful API endpoints for it.

### Creating  JPA Entity

I have used a simple blog application in the past years  to demonstrate different frameworks. In this post, I will reuse the blog application concept.  

Basically it includes two JPA entities, `Post` and `Comment`, it is an one-to-many relation.

Firstly let's have a look at the `Post`  entity class.

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
```
An JPA entity should  be annotated with an  `@Entity` annotation,  optionally, adding a `@Table` to specify the table metadata.

An entity should include a none-arguments constructor. 

An entity should have an identifier field with an `@Id` annotation. To assign a value to the id field automatically, you can select a strategy type by specifying the `strategy` attribute of  the `@GgeneratedValue` annotation,  it could be `AUTO`, `IDENTITY`, `SEQUENCE` and `TABLE`, else you can define your own  generator by set the value of `generator` attribute. In the above `Post` entity, we use the Hibernate built-in `uuid2` strategy to generate a UUID value and assign it  to the id field before persisting.

With the annotations from  Lombok project, eg. `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor` and `@Builder`, it helps you to erase the tedious Java Bean properties,  and keep your codes clean. When building the project, Lombok annotation processor will participate into the compiling progress and generate getters and setters, varied constructors, and a builder class used to create an entity.

Use IDE to generate `equals` and `hasCode` according to the business requirements. 

> Be careful of using Lombok @Data to generate all facilities, especially in the entity in an inheritance structure or containing custom `equals` and `hasCode`  to identify an entity.

Similar to the `Post` entity, create anther entity named  `Comment`.

```java
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
The `Post` and `Comment` association is a simple bi-direction one-to-many relation. 

On the **one** side, aka  in`Post` entity,  a `@OneToMany` annotation is added to the *comments* which is a `List`,  the `cascade` attribute defines the behiavor to process the **many** side when performing a persist, merge, delete operation on  the **one** side, here we use `ALL` to setup all cascade rules will be applied.  The  `orphanRemoval=true` setting tells the persistence context to clear the `Comment` orphans when deleting a `Post` . The `@OrderColumn` will persist the inserted position of comments. 
On the **many** side aka in the `Comment` entity, a `@ManyToOne` annotation is added on the `post` field. The `@JoinColumn` set the column which stores the foreign key constraints by the `Post` id .

Besides one-tomany relation (`@OneToMany`and `@ManyToOne`), JPA specification includes two other relations, aka  one-to-one (`@OneToOne`)  and many-to-many (`@ManyToMany`).

We've just demonstrated a simple entity association case here, it is bi-direction one-to-many relation.  Please note, one-to-one, one-to-many, and many-to-many can be set as *single direction*,  and you can use a  secondary table as *connecting table* in the  one-to-one and one-to-many relations.

> We can not cover every details of JPA specifiction here. If you are new to JPA, [Java persistence with Hibernate](https://www.manning.com/books/java-persistence-with-hibernate) is a good book to start your  JPA journey.

### Creating Repository

Create a `Repository` for the `Post` entity.

```java
@Repository
public interface PostRepository extends JpaRepository<Post, UUID>{

}
```

The `JpaRepository` overrides some existing methods in the  parent`CrudRepository`and `PageableRepository`, and adds some JPA specific methods, such as `flush` used to flush the persistence context by force.

Note, in Micronaut Data, a Repository bean must be annotated with  a `@Repository` annotation. In a multi-datasources environment, you can specify the datasource identifier name to ensure this repository to use certain DataSource to connect to database. 

For example, `@Repository("orders")` to connect the **orders** datasource defined in the *application.yml* configuration.

```yml
datasources:
  orders:
    ....
```

Similarly, create a `Repository` for the `Comment` entity.

```java
@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByPost(Post post);
}
```

The `findByPost`is used to filter comments by a specific *post* argument. Similar to Spring Data JPA,  Micronaut Data support fluent query methods derived from property expression.

Similar to Spring Data, Micronaut Data provides pagination for long query result, the `findAll` accepts a `Pageable` parameter, and returns a `Page` result.

Micronaut Data also includes a `Specification`  to adopt JPA Criteria APIs for complex type-safe query.

### Query by Specification

Change `PostRepository`  , add `JpaSpecificationExecutor<Post>` to extends list.

```java
@Repository
public interface PostRepository extends JpaRepository<Post, UUID>, JpaSpecificationExecutor<Post> {

}
```

The `JpaSpecificationExecutor` provides extra methods to accept a `Specification` as parameters.

Create a specific `PostSpecifications` to group all specifications for querying posts.  Currently only add one for query by keyword and status.

```java
public class PostSpecifications {
    private PostSpecifications(){
        // forbid to instantiate
    }

    public static Specification<Post> filterByKeywordAndStatus(
            final String keyword,
            final Status status
    ) {
        return (Root<Post> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                predicates.add(
                        cb.or(
                                cb.like(root.get(Post_.title), "%" + keyword + "%"),
                                cb.like(root.get(Post_.content), "%" + keyword + "%")
                        )
                );
            }

            if (status != null) {
                predicates.add(cb.equal(root.get(Post_.status), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

The `filterByKeywordAndStatus` specificaiton provides optional keyword and status to filter the posts.  The `Post_` is a metadata class generated by Hibernate metadata generating tools.  

Add the following `annotationProcessor` in the project dependencies.

```groovy 
annotationProcessor('org.hibernate:hibernate-jpamodelgen:5.6.5.Final')
```

For those who are familiar with JPA `EntityManager` and prefer to use literal query string to handle complex queries,  In Micronaut Data, it is easy  to use them in the Repository directly.

###  Custom Query with EntityManager 

Change the Repository interface to an *abstract class*,  and inject an `EntityManager`,  then you can use it freely in your custom methods.

```java
@Repository()
@RequiredArgsConstructor
public abstract class PostRepository implements JpaRepository<Post, UUID>, JpaSpecificationExecutor<Post> {
    private final EntityManager entityManager;

    public List<Post> findAllByTitleContains(String title) {
        return entityManager.createQuery("FROM Post AS p WHERE p.title like :title", Post.class)
                .setParameter("title", "%" + title + "%")
                .getResultList();
    }
}
```

In the above `findAllByTitleContains` method, it uses `EntityManager` to execute a custom literial query.

In contrast, to use `EntityManager`in your custom queries,  in Spring Data JPA, you  need to create a `PostRepositoryCustom` interface and a `PostRepositoryImpl` implementation class. Obviously Micronaut Data simplifies the work.


### Initializing Sample Data

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

In the above codes, use `TransactionOperations` to wrap a serise of operations into a transaction to make sure it happens before the sucessor operations.


## Exposing RESTful API

Follow the REST conventions and Richardson Mature Model,  we design a series of HTTP API endpoints that satisfies the  Richardson Mature Model Level 2.

| URI                  | HTTP Method | Description                                                  |
| -------------------- | ----------- | ------------------------------------------------------------ |
| /posts               | GET         | Get all posts                                                |
| /posts/{id}          | GET         | Get a single Post, if not found return 404 status code.      |
| /posts               | POST        | Create a new Post, if successful, return 201 and add newly-created Post URI to the response `Location` header |
| /posts/{id}          | PUT         | Update the existing Post, return 204. If not found return 404 |
| /posts/{id}          | DELETE      | Delete the existing Post, return 204. If not found return 404 |
| /posts/{id}/comments | GET         | Get all comments of the specified Post.                      |
| ...                  |             |                                                              |

Similar to Spring WebMVC,  Micronaut uses a `Controller` to expose Restful APIs. 

### Creating Controller

Create a controller to produce RESTful API.

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

A controller is annotated with `@Controller`, you can set a base `uri` that can be applied on all methods. 

> Note,  There is no `@RestController` in Micronaut.

The `@Get`, `@Post`,`@Put`, `@Delete` annotations are used to handle varied HTTP methods, it is similar to the Spring's `@GetMapping`, `@PostMapping`, etc.  

You can set media types using *consumes* or *produces*  attributes in these annotations to limit the request and response  content type, or use extra standalone annotations `@Consumes` and `@Produces` on the methods.

In the `PostController` , we have two methods.  The `getAll` method serves the */posts* endpoint, and the `getById(id)` serves the */posts/{id}* endpoint.

### Testing Endpoints using cURL

Start up  the application via Gradle command.

```bash 
./gradlew run
```

> Do not forget  to start up Postgres database firstly.

Open a terminal,  use `curl` command to test the `/posts` endpoint.

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

> Micronaut CLI provides commands to generate skeleton for controller, repository, bean, test, etc.  Run `mn --help` in **the project root folder** to get all available commands.

### Handling Exception 

In the above `PostController`, if there is no posts found for the given post id, it return a 404 HTTP status directly.  In a real world application, we can use an custom exception to envelope the exception case. 

Like Spring WebMVC, Micronaut provides similar exception handling mechanism. 

For example, create an `PostNotFoundException` to stand for the case if the post was not found by a specified id.

Create a `PostNotFoundException` class. 

```java
public class PostNotFoundException extends RuntimeException 
    public PostNotFoundException(UUID id) {
        super("Post[id=" + id + "] was not found");
    }
}
```

In the `PostController`,  throw this exception in the `Optional.orElseThrow` block.

```java
@Get(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
public HttpResponse<?> getById(@PathVariable UUID id) {
    return posts.findById(id)
        .map(p -> ok(new PostDetailsDto(p.getId(), p.getTitle(), p.getContent(), p.getStatus(), p.getCreatedAt())))
        .orElseThrow(() -> new PostNotFoundException(id));
}
```

Add a `PostNotFoundExceptionHandler` to handle `PostNotFoundException`.

```java
@Produces
@Singleton
@Requires(classes = { PostNotFoundException.class})
@RequiredArgsConstructor
public class PostNotFoundExceptionHandler implements ExceptionHandler<PostNotFoundException, HttpResponse<?>> {
    private final ErrorResponseProcessor<?> errorResponseProcessor;

    @Override
    public HttpResponse<?> handle(HttpRequest request, PostNotFoundException exception) {
        return errorResponseProcessor.processResponse(
                ErrorContext.builder(request)
                        .cause(exception)
                        .errorMessage(exception.getMessage())
                        .build(),
                HttpResponse.notFound()
        );
    }
}
```

Open your terminal,  use `curl` command to test the `/posts/{id}` endpoint with an none-existing id.

```bash
# curl http://localhost:8080/posts/b6fb90ab-2719-498e-a5fd-93d0c7669fdf -v
> GET /posts/b6fb90ab-2719-498e-a5fd-93d0c7669fdf HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.55.1
> Accept: */*
>
< HTTP/1.1 404 Not Found
< Content-Type: application/json
< date: Mon, 25 Oct 2021 07:02:01 GMT
< content-length: 301
< connection: keep-alive
<
{
  "message" : "Not Found",
  "_links" : {
    "self" : {
      "href" : "/posts/b6fb90ab-2719-498e-a5fd-93d0c7669fdf",
      "templated" : false
    }
  },
  "_embedded" : {
    "errors" : [ {
      "message" : "Post[id=b6fb90ab-2719-498e-a5fd-93d0c7669fdf] was not found"
    } ]
  }
}
```



### Handling Pagination

Change the `getAll` method of `PostController` to the following.

```java
@Get(uri = "/", produces = MediaType.APPLICATION_JSON)
@Transactional
public HttpResponse<Page<PostSummaryDto>> getAll(@QueryValue(defaultValue = "") String q,
                                                 @QueryValue(defaultValue = "") String status,
                                                 @QueryValue(defaultValue = "0") int page,
                                                 @QueryValue(defaultValue = "10") int size) {
    var pageable = Pageable.from(page, size, Sort.of(Sort.Order.desc("createdAt")));
    var postStatus = StringUtils.hasText(status) ? com.example.domain.Status.valueOf(status) : null;
    var data = this.posts.findAll(PostSpecifications.filterByKeywordAndStatus(q, postStatus), pageable);
    var body = data.map(p -> new PostSummaryDto(p.getId(), p.getTitle(), p.getCreatedAt()));
    return ok(body);
}
```

All the query parameters are optional. 

Let's use `curl` to test the */posts* endpoint again.

```bash
# curl http://localhost:8080/posts
{
  "content" : [ {
    "id" : "c9ec963d-2df5-4d65-bfbe-5a0d4cb14ca6",
    "title" : "Getting started wit Micronaut",
    "createdAt" : "2021-10-25T16:35:03.732951"
  }, {
    "id" : "0a79185c-5981-4301-86d1-c266b26b4980",
    "title" : "Getting started wit Micronaut: part 2",
    "createdAt" : "2021-10-25T16:35:03.732951"
  } ],
  "pageable" : {
    "number" : 0,
    "sort" : {
      "orderBy" : [ {
        "property" : "createdAt",
        "direction" : "DESC",
        "ignoreCase" : false,
        "ascending" : false
      } ],
      "sorted" : true
    },
    "size" : 10,
    "offset" : 0,
    "sorted" : true,
    "unpaged" : false
  },
  "totalSize" : 2,
  "totalPages" : 1,
  "empty" : false,
  "size" : 10,
  "offset" : 0,
  "numberOfElements" : 2,
  "pageNumber" : 0
}
```

The `Page`  JSON results looks a little tedious,  let's customize a  Jackson `JsonSerializer` to clean up the JSON data str.

### Customizing JsonSerializer

Create a `PageJsonSerializer` to process the `Page` object as you expected.

```java
@Singleton
public class PageJsonSerializer extends JsonSerializer<Page<?>> {
    @Override
    public void serialize(Page<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("pageNumber", value.getPageNumber());
        if (value.getNumberOfElements() != value.getSize()) {
            //only display it in the last page when number of elements is not equal to page size.
            gen.writeNumberField("numberOfElements", value.getNumberOfElements());
        }
        gen.writeNumberField("size", value.getSize());
        gen.writeNumberField("totalPages", value.getTotalPages());
        gen.writeNumberField("totalSize", value.getTotalSize());
        gen.writeObjectField("content", value.getContent());
        gen.writeEndObject();
    }
}

```

Run the application , and hint  */posts* endpoint again.

```bash
# curl http://localhost:8080/posts
{
  "pageNumber" : 0,
  "numberOfElements" : 2,
  "size" : 10,
  "totalPages" : 1,
  "totalSize" : 2,
  "content" : [ {
    "id" : "53fb77d5-4159-4a80-bab9-c76d9a535b36",
    "title" : "Getting started wit Micronaut",
    "createdAt" : "2021-10-25T16:47:05.545594"
  }, {
    "id" : "aa02fd49-0c24-4f12-b204-2e48213c7a1e",
    "title" : "Getting started wit Micronaut: part 2",
    "createdAt" : "2021-10-25T16:47:05.545594"
  } ]
}
```

## Creating Post

 We have discussed how to query posts by key word and get single post by id,  in this section, we will focus on creating a new post.

According the REST convention, we will use a POST HTTP method  to send a request on endpoint */posts*, it accepts JSON data as request body. 

```
@io.micronaut.http.annotation.Post(uri = "/", consumes = MediaType.APPLICATION_JSON)
@Transactional
public HttpResponse<Void> create(@Body CreatePostCommand dto) {
    var data = Post.builder().title(dto.title()).content(dto.content()).build();
    var saved = this.posts.save(data);
    return HttpResponse.created(URI.create("/posts/" + saved.getId()));
}
```

The request body is deserialized as a POJO by built-in Jackson `JsonDesearilizer`s, it is annotated with a `@Body` annotation to indicate which target class it will be desearilized to.  After the post data is saved, set the response header `Location` value to the URI of the newly created post.

Run the application, and try to add a post via `curl`, and then access the newly created post.

```bash
# curl -X POST -v  -H "Content-Type:application/json" http://localhost:8080/posts -d "{\"title\":\"test title\",\"content\":\"test content\"}"
> POST /posts HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.55.1
> Accept: */*
> Content-Type:application/json
> Content-Length: 47
>
* upload completely sent off: 47 out of 47 bytes
< HTTP/1.1 201 Created
< location: /posts/7db15639-62e3-4d3e-9cf4-f54413502ea6
< date: Mon, 25 Oct 2021 09:07:40 GMT
< connection: keep-alive
< transfer-encoding: chunked
<
# curl http://localhost:8080/posts/7db15639-62e3-4d3e-9cf4-f54413502ea6
{
  "id" : "7db15639-62e3-4d3e-9cf4-f54413502ea6",
  "title" : "test title",
  "content" : "test content",
  "status" : "DRAFT",
  "createdAt" : "2021-10-25T17:07:40.87621"
}
```

### Validating Request Body

Generally, in a real world application, we have to ensure the request data satisfies requirements. Micronaut has built-in Bean Validation support.

In the above `CreatPostCommand` class, add Bean Validation annotations on the fields.

```java
@Introspected
public record CreatePostCommand(@NotBlank String title, @NotBlank String content) {
}
```

You have to add `@Introspected` annotation to let Micronaut plugin to preprocess bean validation annotations at compile time, thus Bean Validation will work without any Java Reflection APIs at runtime time.

Add a `@Validated` on the `PostController` class to enable validation in the whole class.  

The add a `@Valid` on the method argument which presents the request body.

```java
@Validated
public class PostController {
    public HttpResponse<Void> create(@Body @Valid CreatePostCommand dto) {...}
    //...
}
```

Open a terminal, try to create a Post with a empty  *content* field.

```bash
curl -X POST -v  -H "Content-Type:application/json" http://localhost:8080/posts -d "{\"title\":\"test title\",\"content\":\"\"}"
> POST /posts HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.55.1
> Accept: */*
> Content-Type:application/json
> Content-Length: 35
>
* upload completely sent off: 35 out of 35 bytes
< HTTP/1.1 400 Bad Request
< Content-Type: application/json
< date: Mon, 25 Oct 2021 09:23:22 GMT
< content-length: 237
< connection: keep-alive
<
{
  "message" : "Bad Request",
  "_embedded" : {
    "errors" : [ {
      "message" : "dto.content: must not be blank"
    } ]
  },
  "_links" : {
    "self" : {
      "href" : "/posts",
      "templated" : false
    }
  }
}
```

## Deleting Post

According to REST convention, to delete a single post, send a `DELETE` request on `/posts/{id}`, if it is successful, returns a 204 status. If the `id` is not existed, returns a `404` instead.

Add the following codes to the `PostController`.

```java
@Delete(uri = "/{id}", produces = MediaType.APPLICATION_JSON)
@Transactional
public HttpResponse<?> deleteById(@PathVariable UUID id) {
    return posts.findById(id)
        .map(p -> {
            this.posts.delete(p);
            return HttpResponse.noContent();
        })
        .orElseThrow(() -> new PostNotFoundException(id));
    //.orElseGet(HttpResponse::notFound);
}
```

## Processing Subresources

In our application, the a `Comment` resource, it should be a subresource of `Post` resource when adding comments or fetching comments of a specific post, we can design comments resource like this.

* `POST /posts/{id}/comments` , add  a `Comment` resource to a specific `Post`.
* `GET /posts/{id}/comments`, get all comments of a certain `Post` which id value is the path variable `id`.

```java
// nested comments endpoints
@Get(uri = "/{id}/comments", produces = MediaType.APPLICATION_JSON)
public HttpResponse<?> getCommentsByPostId(@PathVariable UUID id) {
    return posts.findById(id)
        .map(post -> {
            var comments = this.comments.findByPost(post);
            return ok(comments.stream().map(c -> new CommentDetailsDto(c.getId(), c.getContent(), c.getCreatedAt())));
        })
        .orElseThrow(() -> new PostNotFoundException(id));
    //.orElseGet(HttpResponse::notFound);
}

@io.micronaut.http.annotation.Post(uri = "/{id}/comments", consumes = MediaType.APPLICATION_JSON)
@Transactional
public HttpResponse<?> create(@PathVariable UUID id, @Body @Valid CreateCommentCommand dto) {

    return posts.findById(id)
        .map(post -> {
            var data = Comment.builder().content(dto.content()).post(post).build();
            post.getComments().add(data);
            var saved = this.comments.save(data);
            return HttpResponse.created(URI.create("/comments/" + saved.getId()));
        })
        .orElseThrow(() -> new PostNotFoundException(id));
    // .orElseGet(HttpResponse::notFound);

}
```

## Example Codes

The example codes are hosted on my GitHub, check [hantsy/micronaut-sandbox#post-service](https://github.com/hantsy/micronaut-sandbox/tree/master/post-service).