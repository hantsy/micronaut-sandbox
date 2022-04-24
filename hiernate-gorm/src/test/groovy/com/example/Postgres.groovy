package com.example

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class Postgres {
    static PostgreSQLContainer pgContainer

    static init() {
        if (pgContainer == null) {
            pgContainer = new PostgreSQLContainer(DockerImageName.parse("postgres:12"))
            pgContainer.start()
        }
    }
}
