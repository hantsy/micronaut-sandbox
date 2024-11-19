package com.example;

import io.micronaut.context.ApplicationContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.DockerHealthcheckWaitStrategy;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
class PostRepositoryTest {
    static ApplicationContext applicationContext;
    static PostRepository postRepository;
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("blogdb")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("init.sql"),
                    "/docker-entrypoint-initdb.d/init.sql"
            );

    @BeforeAll
    public static void setupAll() {
        postgreSQLContainer.start();
        postgreSQLContainer.waitingFor(new DockerHealthcheckWaitStrategy().withStartupTimeout(Duration.ofMillis(1000)));
        applicationContext = ApplicationContext
                .run(
                        Map.of(
                                "datasources.default.url", postgreSQLContainer.getJdbcUrl(),
                                "datasources.default.username", postgreSQLContainer.getUsername(),
                                "datasources.default.password", postgreSQLContainer.getPassword(),
                                "vertx.pg.client.host", postgreSQLContainer.getHost(),
                                "vertx.pg.client.port", postgreSQLContainer.getFirstMappedPort(),
                                "vertx.pg.client.user", postgreSQLContainer.getUsername(),
                                "vertx.pg.client.password", postgreSQLContainer.getPassword()
                        )
                )
//                .registerSingleton(PostRepository.class)
//                .registerSingleton(PoolFactory.class)
                .start();
    }

    @AfterAll
    public static void afterAll() {
        if (applicationContext.isRunning()) {
            applicationContext.stop();
        }
        if (postgreSQLContainer.isRunning()) {
            postgreSQLContainer.stop();
        }
    }

    @BeforeEach
    public void setup() {
        log.debug("setup....");
        postRepository = applicationContext.getBean(PostRepository.class);
        assertThat(postRepository).isNotNull();
        postRepository
                .saveAll(
                        List.of(
                                new Post(null, "test", "test content", null),
                                new Post(null, "test2", "test content2", null)
                        )
                )
                .blockingSubscribe();
    }

    @SneakyThrows
    @Test
    public void testFindAll() {
        var countLatch = new CountDownLatch(1);
        var posts = new ArrayList<Post>();
        postRepository.findAll()
                .doOnComplete(countLatch::countDown)
                .subscribe(posts::add);
        countLatch.await(1000, TimeUnit.MILLISECONDS);
        log.debug("all posts: {}", posts);
        assertThat(posts.size()).isEqualTo(2);

        var deleted = postRepository.deleteAll().blockingGet();
        assertThat(deleted).isGreaterThan(0);
    }


    @Test
    public void testInsertAndQuery() {
        var id = postRepository.save(new Post(null, "test", "test content", null))
                .blockingGet();
        assertNotNull(id);

        var found = postRepository.findById(id).blockingGet();
        assertThat(found).isNotNull();
        assertThat(found.createdAt()).isNotNull();
        assertThat(found.content()).isEqualTo("test content");

        var deleted = postRepository.deleteById(id).blockingGet();
        assertThat(deleted).isGreaterThan(0);
    }
}