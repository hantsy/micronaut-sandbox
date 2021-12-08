# Building Micronaut applications with Micronaut Data R2dbc and Kotlin Coroutines

In this post, we will continue to explore Micronaut Data  R2dbc,  and rewrite the previous Data Jdbc/Kotlin example with Data R2dbc and Kotlin Coroutines. In contrast to Jdbc, R2dbc is another RDBMS database connection specification but provides asynchronous none-blocking API for users. R2dbc API is totally compatible with Reactive Streams specification. Kotlin Coroutines is an official Kotlin extension provides an event-loop based asynchronous programming model.



## Getting Started

Open your browser and navigate to [Micronaut Launch](https://micronaut.io/launch) to generate a new project skeleton for this post. Select the following items on this page.

*  Java version:  **17**

*  Language: **Kotlin** 

*  Build tool: **Gradle Kotlin**

*  Test framework: **Kotest**

*  Included Features: **data-r2dbc**, **postgres**, **kotlin-extension-functions** etc.

Click **GENERATE PROJECT** button to generate a project archive, download it and extract the files into disk, and import to your IDE, such as IDEA.

Open *pom.xml* file, add Kotlin Coroutines into the project dependencies.

```kotlin
//kotlin coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
```

The `kotlinx-coroutines-reactor` provides exchanges between the Reactor API and  Kotlin Coroutines API.

Create an Entity mapped to a table in the database.

```kotlin
@MappedEntity(value = "posts", namingStrategy = NamingStrategies.UnderScoreSeparatedLowerCase::class)
data class Post(
    @AutoPopulated//generated value UUID does not work here.
    @field:Id var id: UUID? = null,
    var title: String,
    var content: String,
    var status: Status? = Status.DRAFT,
    @field:DateCreated var createdAt: LocalDateTime? = LocalDateTime.now()
)
```

Micronaut Data R2dbc does not include a `UUID`  ID generator strategy, here we use `@AutoPopulated` to generate a random UUID before persisting into database.

Create a Repository interface for `Post` entity.

```kotlin
@R2dbcRepository(dialect = Dialect.POSTGRES)
interface PostRepository : CoroutineCrudRepository<Post, UUID>, CoroutineJpaSpecificationExecutor<Post>
```

Micronaut Data provides several Repository interface for ReactiveStreams API, for Reactor users, there is `ReactorCrudRepository`. The  `CoroutineCrudRepository` is a Kotlin Coroutines compatible Repository interface which returns a *suspend* result in the functions. 

> The `@R2dbcRepository` requires a `dialect` here, else it will fail at the application startup.

 Similarly, `JpaSpecificationExecutor` has some variants for Reactive Streams, the `CoroutineJpaSpecificationExecutor` is ready for Kotlin Coroutines. We have created a `Specificaitons` to setup several criteria for query, update and delete operations, we will reuse them in this post.

Let's move to the Controller, create a new controller class named  `PostController`.

```kotlin
@Controller("/posts")
class PostController(private val posts: PostRepository) {

    @Get(uri = "/", produces = [MediaType.APPLICATION_JSON])
    fun all(): HttpResponse<Flow<Post>> = ok(posts.findAll())

    @Get(uri = "/{id}", produces = [MediaType.APPLICATION_JSON])
    suspend fun byId(@PathVariable id: UUID): HttpResponse<Any> {
        val post = posts.findById(id) ?: return notFound()
        return ok(post)
    }

    @io.micronaut.http.annotation.Post(consumes = [MediaType.APPLICATION_JSON])
    suspend fun create(@Body body: Post): HttpResponse<Any> {
        val saved = posts.save(body)
        return created(URI.create("/posts/" + saved.id))
    }
}
```

It looks very similar to  Jdbc version we have done in the last post, but here we return a Kotlin Coroutines specific `Flow` type or use a `suspend` function. The difference is all of these methods are executed in an coroutine context.

Now let's try to add some sample data via a `DataInitializer` bean,which listens to a `ServerStartUpEvent`.

```kotlin
@Singleton
class DataInitializer(private val posts: PostRepository) {

    @EventListener//does not support `suspend`
    fun onStartUp(e: ServerStartupEvent) {
        log.info("starting data initialization at StartUpEvent: $e")

        runBlocking {
            val deleteAll = posts.deleteAll()
            log.info("deleted posts: $deleteAll")

            val data = listOf(
                Post(title = "Building Restful APIs with Micronaut and Kotlin Coroutine", content = "test"),
                Post(title = "Building Restful APIs with Micronaut and Kotlin Coroutine: part 2", content = "test")
            )
            data.forEach { log.debug("saving: $it") }
            posts.saveAll(data)
                .onEach { log.debug("saved post: $it") }
                .onCompletion { log.debug("completed.") }
                .flowOn(Dispatchers.IO)
                .launchIn(this);
        }

        log.info("data initialization is done...")
    }

    companion object DataInitializer {
        private val log = LoggerFactory.getLogger(DataInitializer::class.java)
    }

}
```

The `EventListener` does not support a `suspend` function, use a `runBlocking` to block the current thread and run the *suspend* functions in sequence.

##  JPA Criteria API

Micronaut Data provides JPA criteria API support for both Data Jdbc and Data R2dbc, and also add some `JpaSpecificationExecutor` variants for Reactive Streams API, as mentioned in previous sections, there is a `CoroutineJpaSpecificationExecutor` for Kotlin Coroutines.

Add `jakarta-persistence-api` into dependencies to provide JPA Criteria API.

```bash 
implementation("jakarta.persistence:jakarta.persistence-api:3.0.0")
```

Let's reuse `Specifications` we have created in the last post.

Create a test to verify the criteria defined in the `Specifications`.

```kotlin
@MicronautTest(environments = [Environment.TEST], startApplication = false)
class PostRepositoryTest(
    private val posts: PostRepository,
    private val template: R2dbcOperations
) : StringSpec({

    "save and find posts" {
        val sql = "insert into posts(title, content, status) values ($1, $2, $3)";
        Mono
            .from(template.withTransaction { status: ReactiveTransactionStatus<Connection> ->
                Mono.from(
                    status.connection.createStatement(sql)
                        .bind(0, "test title")
                        .bind(1, "test content")
                        .bind(2, "DRAFT")
                        .execute()
                ).flatMap { Mono.from(it.rowsUpdated) }
            })
            .log()
            .`as` { StepVerifier.create(it) }
            .consumeNextWith { it shouldBeEqualComparingTo 1 }
            .verifyComplete()

        runBlocking {
            val all = posts.findAll().toList()
            all shouldHaveSize 1
            log.debug("all posts: $all")
            all.map { it.title }.forAny { it shouldContain "test" }
        }

    }

    "find by title" {
        val sql = "insert into posts(title, content, status) values ($1, $2, $3)";
        Mono
            .from(template.withTransaction { status: ReactiveTransactionStatus<Connection> ->
                Mono.from(
                    status.connection.createStatement(sql)
                        .bind(0, "test title")
                        .bind(1, "test content")
                        .bind(2, "DRAFT")
                        .execute()
                ).flatMap { Mono.from(it.rowsUpdated) }
            })
            .`as` { StepVerifier.create(it) }
            .consumeNextWith { it shouldBeEqualComparingTo 1 }
            .verifyComplete()

        runBlocking {
            val all = posts.findAll(Specifications.titleLike("test")).toList()
            log.debug("all posts size:{}", all.size)
            all shouldHaveSize 1

            val all2 = posts.findAll(Specifications.titleLike("test2")).toList()
            log.debug("all2 posts size:{}", all2.size)
            all2 shouldHaveSize 0
        }

    }

    "find by keyword" {
        val sql = "insert into posts(title, content, status) values ($1, $2, $3)";
        Flux
            .from(template.withTransaction { status: ReactiveTransactionStatus<Connection> ->
                val statement = status.connection.createStatement(sql)
                statement
                    .bind(0, "test title")
                    .bind(1, "test content")
                    .bind(2, "DRAFT")
                    .add()
                statement.bind(0, "test2 title")
                    .bind(1, "test2 content")
                    .bind(2, "DRAFT")
                    .add()

                Flux.from(statement.execute()).flatMap { Flux.from(it.rowsUpdated) }
            })
            .`as` { StepVerifier.create(it) }
            .consumeNextWith { it shouldBeEqualComparingTo 1 }
            .consumeNextWith { it shouldBeEqualComparingTo 1 }
            .verifyComplete()

        runBlocking {
            val all = posts.findAll(Specifications.byKeyword("test")).toList()
            log.debug("all posts size:{}", all.size)
            all shouldHaveSize 2

            val all2 = posts.findAll(Specifications.byKeyword("test2")).toList()
            log.debug("all2 posts size:{}", all2.size)
            all2 shouldHaveSize 1
        }
    }

    "update posts" {
        val sql = "insert into posts(title, content, status) values ($1, $2, $3)";
        Flux
            .from(template.withTransaction { status: ReactiveTransactionStatus<Connection> ->
                val statement = status.connection.createStatement(sql)
                statement
                    .bind(0, "test title")
                    .bind(1, "test content")
                    .bind(2, "PENDING_MODERATED")
                    .add()

                statement
                    .bind(0, "test2 title")
                    .bind(1, "test2 content")
                    .bind(2, "PENDING_MODERATED")
                    .add()

                Flux.from(statement.execute()).flatMap { Flux.from(it.rowsUpdated) }
            })
            .`as` { StepVerifier.create(it) }
            .consumeNextWith { it shouldBeEqualComparingTo 1 }
            .consumeNextWith { it shouldBeEqualComparingTo 1 }
            .verifyComplete()

        runBlocking {
            val updated = posts.updateAll(Specifications.rejectAllPendingModerated())
            log.debug("updated posts size:{}", updated)
            updated shouldBe 2

            val all = posts.findAll().toList()
            all shouldHaveSize 2
            all.map { it.status }.forAny { it shouldBe Status.REJECTED }
        }
    }

    "remove posts" {
        val sql = "insert into posts(title, content, status) values ($1, $2, $3)";
        Flux
            .from(template.withTransaction { status: ReactiveTransactionStatus<Connection> ->
                val statement = status.connection.createStatement(sql)
                statement
                    .bind(0, "test title")
                    .bind(1, "test content")
                    .bind(2, "REJECTED")
                    .add()
                statement
                    .bind(0, "test2 title")
                    .bind(1, "test2 content")
                    .bind(2, "DRAFT")
                    .add()

                Flux.from(statement.execute()).flatMap { Flux.from(it.rowsUpdated) }
            })
            .`as` { StepVerifier.create(it) }
            .consumeNextWith { it shouldBeEqualComparingTo 1 }
            .consumeNextWith { it shouldBeEqualComparingTo 1 }
            .verifyComplete()

        runBlocking {
            val deleted = posts.deleteAll(Specifications.removeAllRejected())
            log.debug("deleted posts size:{}", deleted)
            deleted shouldBe 1

            val all = posts.findAll().toList()
            all shouldHaveSize 1
            all.map { it.status }.forAny { it shouldBe Status.DRAFT }
        }
    }

}) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(PostRepositoryTest::class.java)
    }

    override fun beforeEach(testCase: TestCase) {
        val sql = "delete from posts";

        val latch = CountDownLatch(1)
        Mono
            .from(
                this.template.withConnection { conn: Connection ->
                    Mono.from(conn.beginTransaction())
                        .then(Mono.from(conn.createStatement(sql).execute())
                            .flatMap { Mono.from(it.rowsUpdated) }
                            .doOnNext { log.debug("deleted rows: $it ") }
                        )
                        .then(Mono.from(conn.commitTransaction()))
                        .doOnError { Mono.from(conn.rollbackTransaction()).then() }
                }
            )
            .log()
            .doOnTerminate { latch.countDown() }
            .subscribe(
                { data -> log.debug("deleted posts: $data ") },
                { error -> log.error("error of cleaning posts: $error") },
                { log.info("done") }
            )

        latch.await(5000, TimeUnit.MILLISECONDS)
    }
}
```

We converted the existing Jdbc version to R2dbc, there are some mainly difference.

* Similar to the blocking `TransactionOperations`, `R2dbcOperations` provides `withConnection` and `withTransaction` to wrap data operations within a connection or transaction boundary.
* R2dbc `Connection` is based on ReactiveStreams API.
* When binding parameters to the SQL statement, the parameter indices start with **0**.
* The SQL parameter placeholders are dependent on the database itself, for example, Postgres use `$1`, `$2`...

### Testing Controller

In this post, we still use Kotest as testing framework, as you see in the above `PostRepositoryTest`,  we use a `runBlocking` to wrap the coroutines execution in a blocking context.

The `kotlinx-coroutines-test` provides some helpers to simplify the testing of Kotlin Coroutines, eg. `runBlockingTest`, etc. Add `kotlinx-coroutines-test` into the test dependencies.

```kotlin
//gradle.properties
kotlinCoVersion=1.6.0-RC

//build.gradle.kt
val kotlinCoVersion=project.properties.get("kotlinCoVersoin")

//update versions of kotlin coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${kotlinCoVersion}")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${kotlinCoVersion}")

testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${kotlinCoVersion}")
```



> There is [an issue](https://stackoverflow.com/questions/70243380/test-kotlin-coroutines-with-runblockingtest-failed) to use `runBlockingTest` in a test, make sure you are using the latest 1.6.0-RC, and use `runTest` instead.

Similar to the `runBlocking`, you can use `runTest` to wrap the testing functionality.

```kotlin
@Test
fun `test GET all posts endpoint with runTest`() = runTest {
    val response = client.exchange("/posts", Array<Post>::class.java).awaitSingle()
    response.status shouldBe HttpStatus.OK
    response.body()!!.map { it.title }.forAny {
        it shouldContain "Micronaut"
    }
}
```

> The `runBlockingTest` is deprecated in the latest 1.6.0 version of Kotlin Coroutines.

We can also mock the repository when testing controllers, as we've done in the previous post. Mockk provides some variants for Kotlin Coroutines, such as `coEvery`, `coVerify`, etc.

```kotlin
@MicronautTest(environments = ["mock"])
class PostControllerTest(
    private val postRepository: PostRepository,
    @Client("/") private var client: HttpClient
) : FunSpec({

    test("test get posts endpoint") {
        val posts = getMock(postRepository)
        coEvery { posts.findAll() }
            .returns(
                flowOf(
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

        coVerify(exactly = 1) { posts.findAll() }
    }
}) {
    @MockBean(PostRepository::class)
    fun mockedPostRepository() = mockk<PostRepository>()
}

```

Firstly, create a mock bean for `PostRepository` , then do stubbing with a `coEvery` and verify the calls in the mocks with `coVerify` clause.

Get the complete [source codes](https://github.com/hantsy/micronaut-sandbox/tree/master/r2dbc-kotlin-co) from my Github.









