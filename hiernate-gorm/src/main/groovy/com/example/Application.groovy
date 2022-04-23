package com.example

import groovy.transform.CompileStatic
import io.micronaut.runtime.Micronaut

@CompileStatic
class Application {
    static void main(String[] args) {
        Micronaut.build(args)
                .packages("com.example.model")// scan entities
                .mainClass(Application.class)
                .start()
    }
}