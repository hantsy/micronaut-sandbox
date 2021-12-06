package com.example

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.inspectors.forAny
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.micronaut.context.env.Environment
import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.test.extensions.kotest.annotation.MicronautTest
import io.micronaut.transaction.reactive.ReactiveTransactionStatus
import io.r2dbc.spi.Connection
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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

