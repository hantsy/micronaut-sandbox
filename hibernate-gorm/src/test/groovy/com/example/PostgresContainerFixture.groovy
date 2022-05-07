package com.example

trait PostgresContainerFixture {
    Map<String, Object> getPostgresConfiguration() {
        if (Postgres.pgContainer == null || !Postgres.pgContainer.isRunning()) {
            Postgres.init()
        }
        [
                'dataSource.url'     : Postgres.pgContainer.getJdbcUrl(),
                'dataSource.password': Postgres.pgContainer.getPassword(),
                'dataSource.username': Postgres.pgContainer.getUsername(),
                'dataSource.dbCreate': "create-drop"
        ]
    }
}