package com.example;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FactoryTest {

    @Test
    public void testBeans() {
        var context = ApplicationContext.run();
        var foo = context.getBean(Foo.class);
        var bar = context.getBean(Bar.class);

        assertThat(foo).isNotNull();
        assertThat(bar).isNotNull();
        context.close();
    }
}

@Factory
class FooBar {

    @Singleton
    public Foo foo() {
        return new Foo();
    }

    @Singleton
    public Bar bar() {
        return new Bar();
    }

}

class Foo {
}

class Bar {
}