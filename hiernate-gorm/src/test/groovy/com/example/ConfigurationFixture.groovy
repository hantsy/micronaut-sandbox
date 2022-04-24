package com.example

trait ConfigurationFixture implements PostgresContainerFixture {
    Map<String, Object> getConfiguration() {
        Map<String, Object> m = [:]
        if (specName) {
            m['spec.name'] = specName
        }
        m += postgresConfiguration
        m
    }

    String getSpecName() {
        null
    }
}
