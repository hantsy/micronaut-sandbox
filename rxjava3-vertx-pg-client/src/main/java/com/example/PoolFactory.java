package com.example;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.vertx.rxjava3.sqlclient.Pool;

// see: https://github.com/micronaut-projects/micronaut-sql/issues/1333
@Factory
public class PoolFactory {

    @Bean
    public Pool rx3Pool(io.vertx.sqlclient.Pool pool) {
        return new Pool(pool);
    }
}
