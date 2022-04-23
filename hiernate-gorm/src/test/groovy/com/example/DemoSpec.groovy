package com.example

import com.example.model.Book
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import jakarta.inject.Inject

@MicronautTest(packages = "com.example.model")
class DemoSpec extends Specification {

    @Inject
    EmbeddedApplication<?> application

    def setup(){

    }

    void 'test it works'() {
        expect:
        application.running
    }

}
