package com.example;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.PropertySource;
import io.micronaut.context.env.Environment;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest(application = Application.class, environments = Environment.TEST, startApplication = false)
@PropertySource(value = {@Property(name = "blog.title", value = "test blog")})
class BlogPropertiesTest {

    @Inject
    BlogProperties properties;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testBlogPropertiesInstanceExisted() {
        assertNotNull(this.properties);
    }

    @Test
    void testBlogProperties() {
        assertEquals("test blog", this.properties.title());
        assertEquals("desc in application-test.yml", this.properties.description());
        assertEquals("author in application-test.yml", this.properties.author());
    }
}