package com.example

import io.micronaut.context.annotation.Requires
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
@Requires(notEnv = ["mock"]) // excluded in the mock env
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

    companion object {
        private val log = LoggerFactory.getLogger(DataInitializer::class.java)
    }

}
