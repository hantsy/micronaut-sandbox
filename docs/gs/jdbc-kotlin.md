# Building Micronaut applications with Data Jdbc and Kotlin

Micronaut Data also has great support for Jdbc and R2dbc. In this post, we will explore the Micronaut Data Jdbc and write the example in Kotlin language, and finally we will test the components with Kotest.



## Getting Started

Open your browser and navigate to [Micronaut Launch](https://micronaut.io/launch) to generate a new project skeleton for this post. Select the following items on this page.

*  Java version:  **17**

* Language: **Kotlin** 

* Build tool: **Gradle**

* Test framework: **Kotest**

* Included Features: **data-jdbc**, **postgres**, etc.

Click **GENERATE PROJECT** button to generate a project archive, download it and extract the files into disk, and import to your IDE, such as IDEA.

Create an Entity class.

```kotlin
@MappedEntity(value = "posts", namingStrategy = NamingStrategies.UnderScoreSeparatedLowerCase::class)
data class Post(
    @field:Id @field:GeneratedValue(GeneratedValue.Type.UUID) var id: UUID? = null,
    var title: String,
    var content: String,
    var status: Status? = Status.DRAFT,
    @field:DateCreated var createdAt: LocalDateTime? = LocalDateTime.now()
)
```
Here we declare a Kotlin `data class` to present the data in the mapped table. Similar to the JPA annotations, you can set `ID` and `GeneratedValue` on the field mapped to the primary key of the backend table. Similar to the Auditing feature of Spring Data project, the `createdAt` field annotated with `@DateCreated` will be filled automatically when the entity is being persisted.

The status is an enum class.

```kotlin
enum class Status {
    DRAFT, PENDING_MODERATED, PUBLISHED, REJECTED
}
```



> Note: The `ID` and `GeneratedValue` is from `io.micronaut.data.annotation` package.

Create a `Repository`  for `Post` Entity class.

```kotlin
@JdbcRepository
interface PostRepository : PageableRepository<Post, UUID>
```

Here we used a `JdbcRepository` to indicate this Repository is  a **data-jdbc** `Repository`.

Create a bean to initialize some sample data.

```kotlin
@Singleton
@Requires(notEnv = ["mock"])
class DataInitializer(private val posts: PostRepository) {

    @EventListener
    fun onStartUp(e: ServerStartupEvent) {
        log.info("starting data initialization at ServerStartupEvent: $e")

        posts.deleteAll()

        val data = listOf(
            Post(title = "Building Restful APIs with Micronaut and Kotlin", content = "test"),
            Post(title = "Building Restful APIs with Micronaut and Kotlin: part 2", content = "test")
        )
        data.forEach { log.debug("saving: $it") }
        posts.saveAll(data).forEach { log.debug("saved post: $it") }
        log.info("data initialization is done...")
    }

    companion object DataInitializer {
        private val log = LoggerFactory.getLogger(DataInitializer::class.java)
    }

}

```

Now create a controller to expose RESTful APIs.

```kotlin
@Controller("/posts")
class PostController(private val posts: PostRepository) {

    @Get(uri = "/", produces = [MediaType.APPLICATION_JSON])
    fun all(): HttpResponse<List<Post>> = ok(posts.findAll().toList())

    @Get(uri = "/{id}", produces = [MediaType.APPLICATION_JSON])
    fun byId(@PathVariable id: UUID): HttpResponse<Any> {
        val post = posts.findById(id) ?: return notFound()
        return ok(post)
    }

    @io.micronaut.http.annotation.Post(consumes = [MediaType.APPLICATION_JSON])
    fun create(@Body body: Post): HttpResponse<Any> {
        val saved = posts.save(body)
        return created(URI.create("/posts/" + saved.id))
    }
}
```

Now let's try to start up the application, make sure there is a running Postgres database, the database settings  should match the configuration in the *application.yaml*. 

Simply, you can prepare the database through docker compose file. Run the following command to start a Postgres in docker, the database details is defined in the *docker-compose.yaml*.

```bas
# docker compose up postgres
```

Now run the application.

```bash
# gradlew run 
// or 
# gradlew build
# java build/xxx.jar
```

You can use `curl` command to test the */posts* endpoint.

```bash
# curl http://localhost:8080/posts
```



## Query by Specification

If you have some experience of Spring Data JPA, you will be impressed by the JPA Specification, but it only works with Spring Data JPA. In Micronaut Data, the **data-jdbc** also supports query by JPA Specification. 

Add  `jakarta.persistence:jakarta.persistence-api:3.0.0` into dependencies.

Change `PostRepository`, make it extends `JpaSpecificationExecutor`.

```bas
@JdbcRepository
interface PostRepository : PageableRepository<Post, UUID>, JpaSpecificationExecutor<Post>
```

Create a series of `Specfication`, eg.  find by title, find by keyword, or reject all posts that status is `PENDING_MODERATED`, remove all `REJECTED` posts.  In the Micronaut Data, there are some variants of the `PredicateSpecification`, such as `QuerySpecificaiton`, `UpdateSpecification`,  and `DeleteSpecification`.

```kotlin
object Specifications {

    fun titleLike(title: String): PredicateSpecification<Post> {
        return PredicateSpecification<Post> { root, criteriaBuilder ->
            criteriaBuilder.like(
                root.get("title"),
                "%$title%"
            )
        }
    }

    fun byKeyword(q: String): QuerySpecification<Post> {
        return QuerySpecification<Post> { root, query, criteriaBuilder ->
            criteriaBuilder.or(
                criteriaBuilder.like(root.get("title"), "%$q%"),
                criteriaBuilder.like(root.get("content"), "%$q%")
            )
        }
    }

    fun rejectAllPendingModerated(): UpdateSpecification<Post> {
        return UpdateSpecification<Post> {root, query, criteriaBuilder ->
            query.set(root.get("status"), Status.REJECTED)
            criteriaBuilder.equal(root.get<Status>("status"), Status.PENDING_MODERATED)
        }
    }

    fun removeAllRejected(): DeleteSpecification<Post> {
        return DeleteSpecification<Post> {root, query, criteriaBuilder ->
            criteriaBuilder.equal(root.get<Status>("status"), Status.REJECTED)
        }
    }

}
```

Let's create some tests to verify these Specifications.

```kotlin
@MicronautTest(environments = [Environment.TEST], startApplication = false)
open class PostRepositoryAnnotationSpec() : AnnotationSpec() {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(PostControllerTest::class.java)
    }

    @Inject
    private lateinit var posts: PostRepository

    @Inject
    private lateinit var template: JdbcOperations

    @Inject
    private lateinit var tx: TransactionOperations<Any>

    @BeforeEach
    fun beforeEach() {
        val callback: TransactionCallback<Any, Int> = TransactionCallback { _: TransactionStatus<Any> ->
            val sql = "delete from posts";
            this.template.prepareStatement(sql) {
                it.executeUpdate()
            }
        }

        val cnt = tx.executeWrite(callback)
        println("deleted $cnt");
    }

    @Test
    fun `test save and find posts`() {
        val sql = "insert into posts(title, content, status) values (?, ?, ?)";
        val insertedCnt = template.prepareStatement(sql) {
            it.setString(1, "test title")
            it.setString(2, "test content")
            it.setString(3, "DRAFT")
            it.executeUpdate()
        }

        insertedCnt shouldBeEqualComparingTo 1
        val all = posts.findAll()
        all shouldHaveSize 1
        log.debug("all posts: $all")
        all.map { it.title }.forAny { it shouldContain "test" }
    }

    @Test
    fun `find by title`() {
        val sql = "insert into posts(title, content, status) values (?, ?, ?)";
        val insertedCnt = template.prepareStatement(sql) {
            it.setString(1, "test title")
            it.setString(2, "test content")
            it.setString(3, "DRAFT")
            it.executeUpdate()
        }

        insertedCnt shouldBeEqualComparingTo 1
        val all = posts.findAll(Specifications.titleLike("test"))
        log.debug("all posts size:{}", all.size)
        all shouldHaveSize 1

        val all2 = posts.findAll(Specifications.titleLike("test2"))
        log.debug("all2 posts size:{}", all2.size)
        all2 shouldHaveSize 0
    }

    @Test
    fun `find by keyword`() {
        val sql = "insert into posts(title, content, status) values (?, ?, ?)";
        val insertedCnt = template.prepareStatement(sql) {
            it.setString(1, "test title")
            it.setString(2, "test content")
            it.setString(3, "DRAFT")
            it.addBatch()
            it.setString(1, "test2 title")
            it.setString(2, "test2 content")
            it.setString(3, "DRAFT")
            it.addBatch()
            it.executeBatch()
        }

        insertedCnt.any { it == 1 }
        val all = posts.findAll(Specifications.byKeyword("test"))
        log.debug("all posts size:{}", all.size)
        all shouldHaveSize 2

        val all2 = posts.findAll(Specifications.byKeyword("test2"))
        log.debug("all2 posts size:{}", all2.size)
        all2 shouldHaveSize 1
    }

    @Test
    fun `update posts`() {
        val sql = "insert into posts(title, content, status) values (?, ?, ?)";
        val insertedCnt = template.prepareStatement(sql) {
            it.setString(1, "test title")
            it.setString(2, "test content")
            it.setString(3, "PENDING_MODERATED")
            it.addBatch()
            it.setString(1, "test2 title")
            it.setString(2, "test2 content")
            it.setString(3, "PENDING_MODERATED")
            it.addBatch()
            it.executeBatch()
        }

        insertedCnt.any { it == 1 }
        val updated = posts.updateAll(Specifications.rejectAllPendingModerated())
        log.debug("updated posts size:{}", updated)
        updated shouldBe 2

        val all = posts.findAll()
        all shouldHaveSize 2
        all.map { it.status }.forAny { it shouldBe Status.REJECTED }
    }

    @Test
    fun `remove posts`() {
        val sql = "insert into posts(title, content, status) values (?, ?, ?)";
        val insertedCnt = template.prepareStatement(sql) {
            it.setString(1, "test title")
            it.setString(2, "test content")
            it.setString(3, "REJECTED")
            it.addBatch()
            it.setString(1, "test2 title")
            it.setString(2, "test2 content")
            it.setString(3, "DRAFT")
            it.addBatch()
            it.executeBatch()
        }

        insertedCnt.any { it == 1 }
        val deleted = posts.deleteAll(Specifications.removeAllRejected())
        log.debug("deleted posts size:{}", deleted)
        deleted shouldBe 1

        val all = posts.findAll()
        all shouldHaveSize 1
        all.map { it.status }.forAny { it shouldBe Status.DRAFT }
    }
}
```

Similar to Spring Jdbc and Spring Data Jdbc,  there is a template based `JdbcOperations` bean available for programmatic database operations. In the above testing codes, we used `JdbcOperations` to prepare and clean up sample data for each tests.

In this application, we use Kotest as testing framework. 

Kotest provides a lot of testing code styles, some are inspired by the existing `describe/it` clause from NodeJS ecosystem or ScalaTest. 

The `AnnotationSpec` is similar to the traditional JUnit coding style, for those from JUnit, it is zero learning curve to migrate to Kotest testing framework.



## Kotest

The simplest is `SpringSpec`, use a *string* to describe functionality. Let's rewrite the above testing codes with `StringSepc`.

```kotlin
@MicronautTest(environments = [Environment.TEST], startApplication = false)
class PostRepositoryTest(
    private val posts: PostRepository,
    private val template: JdbcOperations,
    private val tx: TransactionOperations<Any>
) : StringSpec({

    "test save and find posts" {
        val sql = "insert into posts(title, content, status) values (?, ?, ?)";
        val insertedCnt = template.prepareStatement(sql) {
            it.setString(1, "test title")
            it.setString(2, "test content")
            it.setString(3, "DRAFT")
            it.executeUpdate()
        }

        insertedCnt shouldBeEqualComparingTo 1
        val all = posts.findAll()
        all shouldHaveSize 1
        log.debug("all posts: $all")
        all.map { it.title }.forAny { it shouldContain "test" }
    }

    "find by title" {
        val sql = "insert into posts(title, content, status) values (?, ?, ?)";
        val insertedCnt = template.prepareStatement(sql) {
            it.setString(1, "test title")
            it.setString(2, "test content")
            it.setString(3, "DRAFT")
            it.executeUpdate()
        }

        insertedCnt shouldBeEqualComparingTo 1
        val all = posts.findAll(Specifications.titleLike("test"))
        log.debug("all posts size:{}", all.size)
        all shouldHaveSize 1

        val all2 = posts.findAll(Specifications.titleLike("test2"))
        log.debug("all2 posts size:{}", all2.size)
        all2 shouldHaveSize 0
    }

    "find by keyword" {
        val sql = "insert into posts(title, content, status) values (?, ?, ?)";
        val insertedCnt = template.prepareStatement(sql) {
            it.setString(1, "test title")
            it.setString(2, "test content")
            it.setString(3, "DRAFT")
            it.addBatch()
            it.setString(1, "test2 title")
            it.setString(2, "test2 content")
            it.setString(3, "DRAFT")
            it.addBatch()
            it.executeBatch()
        }

        insertedCnt.any { it == 1 }
        val all = posts.findAll(Specifications.byKeyword("test"))
        log.debug("all posts size:{}", all.size)
        all shouldHaveSize 2

        val all2 = posts.findAll(Specifications.byKeyword("test2"))
        log.debug("all2 posts size:{}", all2.size)
        all2 shouldHaveSize 1
    }

    "update posts" {
        val sql = "insert into posts(title, content, status) values (?, ?, ?)";
        val insertedCnt = template.prepareStatement(sql) {
            it.setString(1, "test title")
            it.setString(2, "test content")
            it.setString(3, "PENDING_MODERATED")
            it.addBatch()
            it.setString(1, "test2 title")
            it.setString(2, "test2 content")
            it.setString(3, "PENDING_MODERATED")
            it.addBatch()
            it.executeBatch()
        }

        insertedCnt.any { it == 1 }
        val updated = posts.updateAll(Specifications.rejectAllPendingModerated())
        log.debug("updated posts size:{}", updated)
        updated shouldBe 2

        val all = posts.findAll()
        all shouldHaveSize 2
        all.map { it.status }.forAny { it shouldBe Status.REJECTED }
    }

    "remove posts" {
        val sql = "insert into posts(title, content, status) values (?, ?, ?)";
        val insertedCnt = template.prepareStatement(sql) {
            it.setString(1, "test title")
            it.setString(2, "test content")
            it.setString(3, "REJECTED")
            it.addBatch()
            it.setString(1, "test2 title")
            it.setString(2, "test2 content")
            it.setString(3, "DRAFT")
            it.addBatch()
            it.executeBatch()
        }

        insertedCnt.any { it == 1 }
        val deleted = posts.deleteAll(Specifications.removeAllRejected())
        log.debug("deleted posts size:{}", deleted)
        deleted shouldBe 1

        val all = posts.findAll()
        all shouldHaveSize 1
        all.map { it.status }.forAny { it shouldBe Status.DRAFT }
    }

}) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(PostControllerTest::class.java)
    }

    override fun beforeEach(testCase: TestCase) {
        val callback: TransactionCallback<Any, Int> = TransactionCallback { _: TransactionStatus<Any> ->
            val sql = "delete from posts";
            this.template.prepareStatement(sql) {
                it.executeUpdate()
            }
        }

        val cnt = tx.executeWrite(callback)
        println("deleted $cnt");
    }
}
```



Create a test to test `PostController`, here we use `FunSpec` which wraps tests in a test method block.

```ko
@MicronautTest(environments = ["mock"])
class PostControllerTest(
    private val postsBean: PostRepository,
    @Client("/") private var client: HttpClient
) : FunSpec({

    test("test get posts endpoint") {
        val posts = getMock(postsBean)
        every { posts.findAll() }
            .returns(
                listOf(
                    Post(
                        id = UUID.randomUUID(),
                        title = "test title",
                        content = "test content",
                        status = Status.DRAFT,
                        createdAt = LocalDateTime.now()
                    )
                )
            )
        val response = client.toBlocking().exchange("/posts", Array<Post>::class.java)

        response.status shouldBe HttpStatus.OK
        response.body()!![0].title shouldBe "test title"

        verify(exactly = 1) { posts.findAll() }
    }
}) {
    @MockBean(PostRepository::class)
    fun posts() = mockk<PostRepository>()
}

```

Here we use **mockk** to create a mocked `PostRepository` and the `MockBean` is located in the body of `SpringSpec`. 

The following is an integration example which use `SpringSpec` .

```kotl
@MicronautTest
class IntegrationTests(
    private val application: EmbeddedApplication<*>,
    @Client("/") private val client: HttpClient
) : StringSpec({

    "test the server is running" {
        assert(application.isRunning)
    }

    "test GET /posts endpoint" {
        val response = client.toBlocking().exchange("/posts", Array<Post>::class.java)

        response.status shouldBe HttpStatus.OK
        response.body()!!.map { it.title }.forAny {
            it shouldContain "Micronaut"
        }
    }
})
```



Get the complete [source codes](https://github.com/hantsy/micronaut-sandbox/tree/master/jdbc-kotlin) from my Github.
