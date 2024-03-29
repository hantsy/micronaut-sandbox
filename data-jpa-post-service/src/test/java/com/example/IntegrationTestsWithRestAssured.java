package com.example;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@MicronautTest
@Slf4j
class IntegrationTestsWithRestAssured {

    @Inject
    EmbeddedServer embeddedServer;

    @BeforeEach
    void setUp() {
        RestAssured.port = embeddedServer.getPort();
    }

    @Test
    void serverIsRunning() {
        assertThat(embeddedServer.isRunning()).isTrue();
    }

    @Test
    void testGetAllPosts() {
        //@formatter:off
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/posts")
        .then()
            .contentType(ContentType.JSON)
            .statusCode(200)
            .body(
                    "pageNumber", equalTo(0),
                    "size", equalTo(10),
                    "totalSize", equalTo(2),
                    "totalPages", equalTo(1),
                    "content[0].title", containsString("Micronaut")
            );
        //@formatter:on
    }


}
