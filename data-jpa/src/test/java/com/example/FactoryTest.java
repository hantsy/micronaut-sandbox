package com.example;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
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

    @Test
    public void testRegisterBeans() {
        var context = ApplicationContext.builder()
                .build();
        context.registerSingleton(new FooBar());
        context.start();

        var fooBar = context.getBean(FooBar.class);

        assertThat(fooBar).isNotNull();
        context.stop();
    }

    @Test
    public void testPrototypeBeans() {
        var context = ApplicationContext.run();
        var model = context.getBean(UserModel.class);
        var model2 = context.getBean(UserModel.class);

        assertThat(model == model2).isFalse();
        context.close();
    }
}

class FooBar {
}


@Prototype
class UserModel {
}

@Factory
class FooBarConfiguration {

    @Bean
    public Foo foo() {
        return new Foo();
    }

    @Bean
    public Bar bar() {
        return new Bar();
    }

}

class Foo {
}

class Bar {
}