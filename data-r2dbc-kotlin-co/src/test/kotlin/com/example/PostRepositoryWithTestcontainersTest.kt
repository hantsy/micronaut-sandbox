package com.example

import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.shouldBe
import io.micronaut.context.env.Environment
import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.test.extensions.kotest.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import io.micronaut.transaction.reactive.ReactiveTransactionStatus
import io.r2dbc.spi.Connection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.TestInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.MountableFile
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
@MicronautTest(environments = [Environment.TEST], startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)// required for TestPropertyProvider to reassign properties.
class PostRepositoryWithTestcontainersTest(
    private val posts: PostRepository,
    private val comments: CommentRepository,
    private val template: R2dbcOperations
) : TestPropertyProvider, StringSpec({

    "find by title" {
        val sql = "insert into posts(title, content, status) values ($1, $2, $3)";
        Mono
            .fromDirect(template.withTransaction { status: ReactiveTransactionStatus<Connection> ->
                Mono.from(
                    status.connection.createStatement(sql)
                        .bind(0, "test title")
                        .bind(1, "test content")
                        .bind(2, "DRAFT")
                        .execute()
                ).flatMap { Mono.from(it.rowsUpdated) }
            })
            .`as` { StepVerifier.create(it) }
            .consumeNextWith { it shouldBe 1L }
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

}) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(PostRepositoryWithTestcontainersTest::class.java)
        private val postgreSQLContainer: PostgreSQLContainer<*> = PostgreSQLContainer<Nothing>("postgres:12")
            .withCopyToContainer(
                MountableFile.forClasspathResource("init.sql"),
                "/docker-entrypoint-initdb.d/init.sql"
            )
    }

    override fun getProperties(): MutableMap<String, String> {
        log.debug("call TestPropertyProvider.getProperties...")
        return mutableMapOf<String, String>(
            "r2dbc.datasources.default.url" to "r2dbc:postgresql://${postgreSQLContainer.host}:${postgreSQLContainer.firstMappedPort}/${postgreSQLContainer.databaseName}",
            "r2dbc.datasources.default.username" to postgreSQLContainer.username,
            "r2dbc.datasources.default.password" to postgreSQLContainer.password
        )
    }

    override fun beforeSpec(spec: Spec) {
        log.debug("call beforeSpec...")
        postgreSQLContainer.start()
    }

    override fun beforeEach(testCase: TestCase) {
        log.debug("call beforeEach...")
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