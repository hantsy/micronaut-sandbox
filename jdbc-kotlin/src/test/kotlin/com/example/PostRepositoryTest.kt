package com.example

import io.kotest.core.spec.style.StringSpec
import io.kotest.inspectors.forAny
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.string.shouldContain
import io.micronaut.context.env.Environment
import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.micronaut.test.extensions.kotest.annotation.MicronautTest
import org.slf4j.LoggerFactory

@MicronautTest(environments = [Environment.TEST], startApplication = false)
class PostRepositoryTest(private val posts: PostRepository, private val template: JdbcOperations) : StringSpec({
    val log = LoggerFactory.getLogger(PostControllerTest::class.java)

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
        all shouldHaveSize 3
        log.debug("all posts: $all")
        all.map { it.title }.forAny { it shouldContain "test" }
    }

})

