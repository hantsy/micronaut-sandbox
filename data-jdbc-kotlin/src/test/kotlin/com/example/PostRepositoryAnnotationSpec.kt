package com.example

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.TestCase
import io.kotest.inspectors.forAny
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.micronaut.context.env.Environment
import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.micronaut.test.extensions.kotest.annotation.MicronautTest
import io.micronaut.transaction.TransactionCallback
import io.micronaut.transaction.TransactionOperations
import io.micronaut.transaction.TransactionStatus
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@MicronautTest(environments = [Environment.TEST], startApplication = false)
open class PostRepositoryAnnotationSpec : AnnotationSpec() {
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
        println("deleted $cnt")
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