package com.example


import com.example.photos.Album
import com.example.photos.AlbumRepository
import groovy.util.logging.Slf4j
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.spock.Testcontainers
import reactor.test.StepVerifier
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@MicronautTest(startApplication = false)
@Testcontainers
@Slf4j
class AlbumRepositorySpec extends Specification implements TestPropertyProvider {

    @Shared
    @AutoCleanup
    MongoDBContainer mongo = new MongoDBContainer("mongo:4")

    @Override
    Map<String, String> getProperties() {
        if (!mongo.isRunning()) {
            mongo.start()
        }
        return ["mongodb.uri": mongo.getReplicaSetUrl()]
    }

    @Inject
    EmbeddedApplication<?> application

    @Inject
    AlbumRepository albumRepository

    def setup() {
        CountDownLatch latch = new CountDownLatch(1)
        albumRepository.deleteAll()
                .doOnTerminate(_ -> latch.countDown())
                .subscribe(it -> log.debug "deleted albums: {}", it)
        latch.await(1000, TimeUnit.MILLISECONDS)
    }

    void 'application is not running'() {
        expect:
        !application.running
    }

    void 'test findAll'() {
        given:
        this.albumRepository.insertMany(List.of(Album.of("Guangzhou"), Album.of("Shanghai")))
                .block(Duration.ofMillis(5000))

        when:
        def result = this.albumRepository.findAll().log()

        then:
        StepVerifier.create(result)
                .expectNextMatches(it -> it.name == "Guangzhou")
                .expectNextMatches(it -> it.name == "Shanghai")
                .expectComplete()
                .verify()
    }
}
