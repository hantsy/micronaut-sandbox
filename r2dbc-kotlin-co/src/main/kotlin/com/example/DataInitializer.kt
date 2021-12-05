package com.example

import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

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
