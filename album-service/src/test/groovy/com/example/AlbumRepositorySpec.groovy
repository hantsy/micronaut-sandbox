package com.example


import com.example.photos.Album
import com.example.photos.AlbumRepository
import groovy.util.logging.Slf4j
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import reactor.test.StepVerifier
import spock.lang.Specification

@MicronautTest(startApplication = false)
@Slf4j
class AlbumRepositorySpec extends Specification {

    @Inject
    EmbeddedApplication<?> application

    @Inject
    AlbumRepository albumRepository;

    def setup() {
        albumRepository.deleteAll().subscribe(it -> log.debug "deleted albums: {}", it)
    }

    void 'application is not running'() {
        expect:
        !application.running
    }

    void 'test findAll'() {
        given:
        this.albumRepository.insertMany(List.of(Album.of("Guangzhou"), Album.of("Shanghai"))).subscribe()

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
