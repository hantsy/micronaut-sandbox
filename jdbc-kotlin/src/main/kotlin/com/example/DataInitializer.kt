package com.example

import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class DataInitializer(private val posts: PostRepository) {

    @EventListener
    fun onStartUp(e: StartupEvent) {
        log.info("starting data initialization at StartUpEvent: $e")

        val deleteAll = posts.deleteAll()
        log.info("deleted posts: $deleteAll")

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
