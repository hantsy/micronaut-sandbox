package com.example.service

import com.example.model.Person
import grails.gorm.services.Service
import grails.gorm.transactions.TransactionService
import io.micronaut.context.annotation.Value
import jakarta.annotation.PostConstruct
import jakarta.inject.Inject
import jakarta.inject.Singleton

import javax.sql.DataSource

@Service(Person)
@Singleton
abstract class PersonService {

    @Inject DataSource dataSource
    @Inject TransactionService transactionService

    @Value('${data-source.db-create}')
    String dbCreate

    boolean initCalled = false

    @PostConstruct
    void init() {
        initCalled = true
    }

    abstract List<Person> list()

    abstract Long count()

    def findByNickNames(String name) {
        Person.all.find {
            it.nicknames.any { n -> n.contains(name)}
        }
    }
}
